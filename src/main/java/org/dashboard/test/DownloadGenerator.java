/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.test;

import org.dashboard.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.namespace.QName;
import org.dashboard.core.collector.DataCollector;
import org.dashboard.core.collector.EntityCounter;

import org.dashboard.core.entity.Download;
import org.dashboard.core.entity.DownloadEntity;
import org.dashboard.core.entity.EntityBaseBean;

import org.dashboard.core.entity.Entity_;
import org.dashboard.core.entity.ICATUser;
import org.dashboard.core.manager.DashboardException;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.ICATSessionManager;
import org.dashboard.core.manager.PropsManager;
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

@Singleton
@DependsOn("ICATSessionManager")
public class DownloadGenerator {

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    @EJB
    private PropsManager prop;
    
     @EJB
    private EntityBeanManager beanManager;
    
    protected ICAT icat;
    protected String sessionID;
    
    Download d = new Download();

    @EJB
    private ICATSessionManager session;
    
    private long downloadSize = 0;

    @PostConstruct
    private void init() {

        icat = createICATLink();
        sessionID = session.getSessionID();
        //To be removed. Used to generate random fake data.
        for(int i=0; i<5; i++){
            try {
                createData();
            } catch (DashboardException ex) {
                Logger.getLogger(DownloadGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void createData() throws DashboardException {      
        d = new Download();
        downloadSize= 0;
        ICATUser user = manager.find(ICATUser.class , new Long(randomGen(44603,44701 )));
        d.preparePersist();
        d.setUser(user);        
        JSONObject obj = createJSON(randomGen(0,20));
        d.setDownloadEnties(createDownloadEntities(obj.toJSONString()));
        d.setSize(downloadSize);
        d.setDownloadTime(randomDate());
        d.setMethod(randomMethod());
        beanManager.create(d, manager);

    }
    
    public Date randomDate(){
        LocalDate date = LocalDate.of(randomGen(1999,2015),randomGen(1,12),randomGen(1,25));     
        
        return java.sql.Date.valueOf(date);
        
    }
    private Entity_ checkEntity(Long ID,String type){ 
        List<Object> en = beanManager.search("SELECT en FROM Entity_ en WHERE en.type='"+type+"' AND en.ICATID="+ID, manager);       
        return (Entity_) en.get(0);
    }
    public String randomMethod(){
        ArrayList<String> methods = new ArrayList();
        methods.add("https");
        methods.add("http");
        methods.add("Globus");
        int ran = randomGen(0,2);
        return methods.get(ran);
    }
    
    public JSONObject createJSON(int size){
        JSONObject obj = new JSONObject();
        JSONArray df = new JSONArray();
        JSONArray inv = new JSONArray();
        JSONArray ds = new JSONArray();
        
        for(int i=0;i<size;i++){
            df.add(randomGen(1,1000));
            inv.add(randomGen(1,1000));
            ds.add(randomGen(1,1000));
        }
        
        obj.put("datafileIds", df);
        obj.put("datasetIds",ds);
        obj.put("investigationIds",inv);
        
        return obj;
    }

    public int randomGen(int min, int max) {
        int randomNum;
        Random rand = new Random();

        randomNum = min + rand.nextInt((max - min) + 1);

        return randomNum;
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
                    de.setDownload(d);
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
                    de.setDownload(d);
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
                    de.setDownload(d);
                    Long id = Long.parseLong(dfIDs.get(i).toString());
                    ent = createEntity(id, "datafile");                    
                    de.setEntity(ent);
                    de.preparePersist();
                    collection.add(de);
                    
                }
            }

            

        } catch (ParseException ex) {
            Logger.getLogger(DownloadGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return collection;

    }

    private Entity_ createEntity(Long id, String EntityType) throws DashboardException {
        Entity_ entity = new Entity_();
        switch (EntityType) {
            case "investigation":
                entity = checkEntity(id,"investigation");
                if(entity == null){
                    Investigation inv = getInvestigation(id);
                    entity.setICATID(id);
                    entity.setEntityName(inv.getName());
                    entity.setICATcreationTime(inv.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(getEntitySize("investigation",id));
                    downloadSize += entity.getEntitySize();
                    entity.setType("investigation");
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;
                
            case "dataset":
                entity = checkEntity(id,"dataset");
                if(entity == null){
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
                entity = checkEntity(id,"datafile");
                if(entity == null){
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
        
            try {
                inv = (Investigation) icat.get(sessionID, "Investigation", id);
            } catch (IcatException_Exception ex) {
                Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
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
}
