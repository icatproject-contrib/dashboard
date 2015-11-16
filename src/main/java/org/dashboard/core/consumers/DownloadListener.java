/**
 * *
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 **
 */
package org.dashboard.core.consumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.net.ssl.HttpsURLConnection;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import org.dashboard.core.collector.DataCollector;
import org.dashboard.core.collector.EntityCounter;
import org.dashboard.core.manager.PropsManager;
import org.dashboard.core.collector.UserCollector;
import org.dashboard.core.entity.Download;
import org.dashboard.core.entity.DownloadEntity;
import org.dashboard.core.entity.Entity_;
import org.dashboard.core.entity.ICATUser;
import org.dashboard.core.entity.Query;
import org.dashboard.core.manager.DashboardException;

import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.ICATSessionManager;
import org.icatproject.Datafile;
import org.icatproject.Dataset;
import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.icatproject.IcatException_Exception;
import org.icatproject.Investigation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DownloadListener implements MessageListener {

    @EJB
    private PropsManager prop;

    @EJB
    private ICATSessionManager session;

    @EJB
    private EntityBeanManager beanManager;

    @EJB
    private UserCollector userCollector;

    @EJB
    private EntityCounter entityCounter;

    @Resource
    private UserTransaction userTransaction;

    protected ICAT icat;

    protected String sessionID;

    private final String api = "/api/v1/admin/downloads/facility/isis?preparedId=";

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ICATListener.class);

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    private Download download;

    private long downloadSize = 0;

    @PostConstruct
    private void init() {
        sessionID = session.getSessionID();
    }

    @Override
    public void onMessage(Message message) {
        downloadSize = 0;
        TextMessage text = (TextMessage) message;

        try {
            download = new Download();
            downloadSize = 0;

            download.preparePersist();
            download.setUser(getUser(message.getStringProperty("user")));

            download.setDownloadEnties(createDownloadEntities(text.getText()));
            download.setSize(downloadSize);
            download.setDownloadTime(new Date());
            download.setMethod(getMethod(message.getStringProperty("preparedID")));
            beanManager.create(download, manager);

        } catch (JMSException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DashboardException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String getMethod(String preparedID) {
        String method = null;
        try {
            URL topCatURL = new URL(prop.getTopCatURL() + api + preparedID);

            HttpsURLConnection httpsConnection = (HttpsURLConnection) topCatURL.openConnection();
            httpsConnection.setRequestMethod("GET");
            httpsConnection.setRequestProperty("Accept", "application/json");
            httpsConnection.addRequestProperty("Authorization", getCredentials());

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpsConnection.getInputStream())));

            String output;
            StringBuffer buffer = new StringBuffer();

            while ((output = responseBuffer.readLine()) != null) {
                buffer.append(output);

            }

            JSONParser parser = new JSONParser();

            Object obj = parser.parse(buffer.toString());
            JSONArray jsonArray = (JSONArray) obj;
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            method = jsonObject.get("transport").toString();

        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        return method;
    }

    public ICATUser getUser(String name) {
        ICATUser dashBoardUser = new ICATUser();
        log.info("Searching for user: " + name + " in dashboard.");
        List<Object> user = beanManager.search("SELECT u FROM USER u WHERE u.name= " + name + "'", manager);

        if (user.get(0) == null) {
            log.info("No user found in the Dashboard. Retrieving from ICAT. ");
            dashBoardUser = userCollector.insertUser(name);

        } else {
            log.info("Found the user in the dashboard");
            dashBoardUser = (ICATUser) user.get(0);
        }

        return dashBoardUser;
    }

    private List<DownloadEntity> createDownloadEntities(String entities) throws DashboardException {
        List<DownloadEntity> collection = new ArrayList();
        Entity_ ent = new Entity_();

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(entities);
            JSONObject json = (JSONObject) obj;

            if (json.containsKey("investigationIds")) {
                JSONArray invIDs = (JSONArray) json.get("investigationIds");
                for (int i = 0; i < invIDs.size(); i++) {
                    DownloadEntity de = new DownloadEntity();
                    de.setDownload(download);
                    Long id = Long.parseLong(invIDs.get(i).toString());
                    ent = createEntity(id, "investigation");
                    de.setEntity(ent);
                    de.preparePersist();
                    collection.add(de);

                }
            }
            if (json.containsKey("datasetIds")) {
                JSONArray dsIDs = (JSONArray) json.get("datasetIds");
                for (int i = 0; i < dsIDs.size(); i++) {
                    DownloadEntity de = new DownloadEntity();
                    de.setDownload(download);
                    Long id = Long.parseLong(dsIDs.get(i).toString());
                    ent = createEntity(id, "dataset");
                    de.setEntity(ent);
                    de.preparePersist();
                    collection.add(de);

                }
            }
            if (json.containsKey("datafileIds")) {
                JSONArray dfIDs = (JSONArray) json.get("datafileIds");
                for (int i = 0; i < dfIDs.size(); i++) {
                    DownloadEntity de = new DownloadEntity();
                    de.setDownload(download);
                    Long id = Long.parseLong(dfIDs.get(i).toString());
                    ent = createEntity(id, "datafile");
                    de.setEntity(ent);
                    de.preparePersist();
                    collection.add(de);

                }
            }

        } catch (ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        return collection;

    }

    private Entity_ createEntity(Long id, String EntityType) throws DashboardException {
        Entity_ entity = new Entity_();
        switch (EntityType) {
            case "investigation":
                entity = checkEntity(id, "investigation");
                if (entity == null) {
                    Investigation inv = getInvestigation(id);
                    entity.setICATID(id);
                    entity.setEntityName(inv.getName());
                    entity.setICATcreationTime(inv.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(getEntitySize("investigation", id));
                    downloadSize += entity.getEntitySize();
                    entity.setType("investigation");
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;

            case "dataset":
                entity = checkEntity(id, "dataset");
                if (entity == null) {
                    Dataset ds = getDataset(id);
                    entity.setICATID(id);
                    entity.setEntityName(ds.getName());
                    entity.setICATcreationTime(ds.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(getEntitySize("dataset", id));
                    downloadSize += entity.getEntitySize();
                    entity.setType("dataset");
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;

            case "datafile":
                entity = checkEntity(id, "datafile");
                if (entity == null) {
                    Datafile df = getDatafile(id);
                    entity.setICATID(id);
                    entity.setEntityName(df.getName());
                    entity.setICATcreationTime(df.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(df.getFileSize());
                    downloadSize += entity.getEntitySize();
                    entity.setType("datafile");
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;
        }
        return entity;
    }

    private Entity_ checkEntity(Long ID, String type) {
        List<Object> en = beanManager.search("SELECT en FROM Entity_ en WHERE en.type='" + type + "' AND en.ICATID=" + ID, manager);
        return (Entity_) en.get(0);
    }

    private Long getEntitySize(String type, Long ID) {

        switch (type) {
            case "investigation":
                return getInvSize(ID);
            case "dataset":
                return getDatasetSize(ID);
        }

        return new Long(0);

    }

    public Investigation getInvestigation(Long id) {
        Investigation inv = null;
        while (inv == null) {
            try {
                inv = (Investigation) icat.get(sessionID, "Investigation", id);
            } catch (IcatException_Exception ex) {
                Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
            }
            id += 1;
        }
        return inv;
    }

    public Dataset getDataset(Long id) {
        Dataset ds = null;

        try {
            ds = (Dataset) icat.get(sessionID, "Dataset", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ds;
    }

    public Datafile getDatafile(Long id) {
        Datafile df = null;

        try {
            df = (Datafile) icat.get(sessionID, "Datafile", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return df;

    }

    public Long getInvSize(Long id) {
        List<Object> s = null;
        try {
            s = icat.search(sessionID, "SELECT SUM(d.fileSize) FROM Datafile d JOIN d.dataset ds JOIN ds.investigation i WHERE i.id=" + id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        Long size = (Long) s.get(0);

        return size;
    }

    public Long getDatasetSize(Long id) {
        List<Object> s = null;
        try {
            s = icat.search(sessionID, "SELECT SUM(d.fileSize) FROM Datafile d JOIN d.dataset ds WHERE ds.id=" + id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        Long size = (Long) s.get(0);

        return size;
    }

    public ICAT createICATLink() {
        ICAT icat = null;
        try {
            URL hostUrl;

            hostUrl = new URL(prop.getICATUrl());
            URL icatUrl = new URL(hostUrl, "ICATService/ICAT?wsdl");
            QName qName = new QName("http://icatproject.org", "ICATService");
            ICATService service = new ICATService(icatUrl, qName);
            icat = service.getICATPort();

        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return icat;
    }

    /**
     * Encodes the topcat admin password and username to Base64
     *
     * @return Base64 topcat credentials
     */
    private String getCredentials() {
        String rawUser = prop.getTopCatUser();
        String rawPass = prop.getTopCatPass();
        String rawCred = rawUser + ":" + rawPass;
        byte[] authEncBytes = Base64.getEncoder().encode(rawCred.getBytes());
        String authStringEnc = new String(authEncBytes);

        return "Basic " + authStringEnc;
    }
}
