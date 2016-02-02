/**
 * *
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 **
 */
package org.icatproject.dashboard.consumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.net.ssl.HttpsURLConnection;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import org.icatproject.dashboard.collector.DataCollector;
import org.icatproject.dashboard.collector.EntityCounter;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.dashboard.collector.UserCollector;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.DownloadEntity;
import org.icatproject.dashboard.entity.DownloadLocation;
import org.icatproject.dashboard.entity.Entity_;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.InternalException;

import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.ICATSessionManager;
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
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "jms/IDS/log"),
    @ActivationConfigProperty(propertyName= "destination", propertyValue="jms_IDS_log"),
    @ActivationConfigProperty(propertyName="acknowledgeMode", propertyValue="Auto-acknowledge"),    
    @ActivationConfigProperty(propertyName="addressList", propertyValue="mq://idsdev2.isis.cclrc.ac.uk:7676"),    
    @ActivationConfigProperty(propertyName = "subscriptionDurability",propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "clientId",propertyValue = "dashboardID"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "dashboardSub"),
    
})

@TransactionManagement(TransactionManagementType.BEAN)
public class DownloadListener implements MessageListener {

    @EJB
    private PropsManager prop;

    @EJB
    private ICATSessionManager sessionManager;

    @EJB
    private EntityBeanManager beanManager;

    @EJB
    private UserCollector userCollector;

    @EJB
    private EntityCounter entityCounter;

    //User transaction is used here to prevent Entities being created if the download fails.
    @Resource
    private UserTransaction ut;

    protected ICAT icat;

    protected String sessionID;

    private final String api = "/api/v1/admin/downloads?preparedId=";

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ICATListener.class);

    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    private Download download;

    private long downloadSize = 0;

    /**
     * Initialises the connection with the ICAT.
     */
    @PostConstruct
    private void init() {
        icat = createICATLink();
        sessionID = sessionManager.getSessionID();
    }
    
    private void ejbCreate(){
        
    }

    /**
     * The implemented method for message listener. Will send the message to the
     * appropriate functions to retrieve the correct data.
     *
     * @param message The message received from the IDS.
     */
    @Override
    public void onMessage(Message message) {

        TextMessage text = (TextMessage) message;
        downloadSize = 0;
        try {
            String operation = text.getStringProperty("operation");
            switch (operation) {
                case "prepareData":
                    prepareDataHandler(text);
                    break;
                case "getData":
                    getDataHandler(text);
                    break;
            }

        } catch (JMSException | ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InternalException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DashboardException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * *
     * Method to deal with prepareDownload messages. Will create the majority of
     * the download information. Creates the download.
     *
     * @param message The message from JMS.
     */
    private void prepareDataHandler(TextMessage message) throws InternalException, ParseException {
        try {
            ut.begin();
            String preparedId = getPreparedId(message.getText());
            download = new Download();
            download.setUser(getUser(message.getText()));
            download.setDownloadEntities(createDownloadEntities(message.getText()));
            download.setDownloadSize(downloadSize);
            download.setMethod(getMethod(preparedId));
            download.setPreparedID(preparedId);
            download.setLocation(getLocation(message.getStringProperty("ip")));            
            beanManager.create(download, manager);
            ut.commit();

        } catch (JMSException | DashboardException | NotSupportedException | SystemException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | RollbackException ex) {
            throw new InternalException(ex.getMessage());
        }
    }

    /**
     * Either creates or updates the download depending on if it is a standard getData call
     * or a getData with a preparedId call.
     *
     * @param message The JMS message that contains the required information.
     */
    private void getDataHandler(TextMessage message) throws InternalException, DashboardException {
        try {
            ut.begin();
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(message.getText());
            JSONObject json = (JSONObject) obj;
            
            if(json.containsKey("preparedId")){
                checkDownload(message);                
            }else{
                createDownload(message);
            }            
           
            ut.commit();
        } catch (JMSException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Deals with the use case of a user downloading the same download order 
     * from TopCat multiple times. If it is a new download then it will update the
     * prepare data download.
     * @param message The JMS Message
     * @throws InternalException
     * @throws JMSException If there is an issue with the JMS message.
     * @throws ParseException Pulling JSON data from the message.
     */
    private void checkDownload(TextMessage message) throws InternalException, JMSException, ParseException, DashboardException {
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(message.getText());
        JSONObject json = (JSONObject) obj;
         
        download = getDownload((String) json.get("preparedId"));
        long duration = message.getLongProperty("millis");   
        long startMilli = message.getLongProperty("start");
        
        //Has happened before so requires a new download.
        if(download.getDownloadEnd()!=null){
            String ipAddress = message.getStringProperty("ip");
            createDownload(download, ipAddress,duration, startMilli);          
        }
        else {
            //Update the prepareData download
                 
            download.setDownloadEnd(new Date(startMilli+duration));
            download.setDownloadStart(new Date(startMilli));
            download.setDuriation(duration);

            beanManager.update(download, manager);
        }
        
    }

    /**
     * Creates the download for a getData call.
     * @param messageBody The JMS message that contains the getData call.
     */
    private void createDownload(TextMessage message) throws InternalException{
        try {            
            long duration = message.getLongProperty("millis");
            long startMilli = message.getLongProperty("start");
            
            
            download = new Download();
            download.setUser(getUser(message.getText()));
            download.setDownloadEntities(createDownloadEntities(message.getText()));
            download.setDownloadSize(downloadSize);            
            download.setLocation(getLocation(message.getStringProperty("ip")));
            download.setMethod("https");
            download.setDuriation(duration);
            download.setDownloadEnd(new Date(startMilli+duration));
            download.setDownloadStart(new Date(startMilli));
            download.setBandwidth(calculateBandwidth(download.getDuriation(),download.getDownloadSize()));
            beanManager.create(download, manager);
        } catch (DashboardException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
                
    }
    /**
     * Overloaded method to handle a user downloading the same download order multiple
     * times
     * @param oldDownload 
     */
    private void createDownload(Download oldDownload, String ipAddress, long duration, long startMilli) throws DashboardException{
        
        download = new Download();        
        download.setUser(oldDownload.getUser());
        download.setPreparedID(oldDownload.getPreparedID());
        download.setDownloadEntities(createDownloadEntities(getEntities(oldDownload.getId())));
        download.setDownloadSize(oldDownload.getDownloadSize());            
        download.setLocation(getLocation(ipAddress));
        download.setMethod(oldDownload.getMethod());
        download.setDuriation(duration);
        download.setDownloadEnd(new Date(startMilli+duration));
        download.setDownloadStart(new Date(startMilli));
        download.setBandwidth(calculateBandwidth(oldDownload.getDuriation(),oldDownload.getDownloadSize()));
        beanManager.create(download, manager);
        
        
    }

    /**
     * Retrieves the preparedId from the message body.
     *
     * @param messageBody The JMS message body
     * @return PreparedId
     */
    private String getPreparedId(String messageBody) throws InternalException {
        String preparedId = null;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(messageBody);
            JSONObject json = (JSONObject) obj;
            
            preparedId = (String) json.get("preparedId");
            
            
        } catch (ParseException ex) {
            throw new InternalException("Unable to parse JMS message body for preparedID");
        }
        
        return preparedId;
    }

    /**
     * Creates a download location object using the GeoTool module.
     *
     * @param ipAddress The idAddress to have it's GeoLocation resolved to.
     */
    private DownloadLocation getLocation(String ipAddress) {
        DownloadLocation location = GeoTool.getDownloadLocation(ipAddress, manager, beanManager);      

        return location;
    }

    /**
     * Calculates the bandwidth in bytes per second
     *
     * @param duration The length of time the download took. 
     * @param size The size of the download.
     * @return bandwidth in bytes per second
     */
    private double calculateBandwidth(long duration, long size) { 
        //Conversion from milliseconds to seconds hence *1000
        double bandwidth = (double) size / ((double) duration*1000);
        
        return bandwidth;
    }

    /**
     * Parses the date into a date object to be inserted into the database.
     *
     * @param stringDate The string to be parsed.
     * @return A date object that contains the value of the string sent.
     */
    private Date parseDate(String stringDate) throws java.text.ParseException {
        Date date;

        return date = format.parse(stringDate);
    }

    /**
     * Retrieves the download from the dashboard database.
     *
     * @param preparedID The unique identifier of the download.
     * @return The download required.
     */
    private Download getDownload(String preparedID) throws InternalException {
        List<Object> download = beanManager.search("SELECT d FROM Download d WHERE d.preparedID='" + preparedID + "'", manager);
        if (download.size() > 0) {
            return (Download) download.get(0);
        }
        //not found
        return null;

    }
    private List<Entity_> getEntities(long downloadID) throws InternalException{
        List<Object> entityQuery = new ArrayList();
        List<Entity_> entities = new ArrayList();
        
        entityQuery = beanManager.search("SELECT e FROM Entity_ e JOIN e.downloadEntities ed JOIN ed.download d WHERE d.id='"+ downloadID +"'", manager);        
        
        for(Object e : entityQuery){
            entities.add((Entity_)e);
        }
        
        return entities;
    }

    /**
     * Gets the download method from the UUID provided from the topcat.
     *
     * @param preparedID The unique download identifier.
     * @return
     */
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
        } catch (IOException | ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        return method;
         
    }

   
    /**
     * Gets the user from the dashboard database. If the user isn't found then
     * they are inserted into the dashboard.
     *
     * @param name The unique name identifier of the user.
     * @return the dashboard user with the provided name.
     */
    public ICATUser getUser(String messageBody) throws InternalException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(messageBody);
        JSONObject json = (JSONObject) obj;

        String name = (String) json.get("userName");

        ICATUser dashBoardUser = new ICATUser();
        log.info("Searching for user: " + name + " in dashboard.");
        List<Object> user = beanManager.search("SELECT u FROM ICATUser u WHERE u.name='" + name + "'", manager);

        if (user.get(0) == null) {
            log.info("No user found in the Dashboard. Retrieving from ICAT. ");
            dashBoardUser = userCollector.insertUser(name);

        } else {
            log.info("Found the user in the dashboard");
            dashBoardUser = (ICATUser) user.get(0);
        }

        return dashBoardUser;
    }

    /**
     * Overloaded method that deals with the use case of a user downloading the 
     * same download order from topCat. This will take existing entities and 
     * create new download entities for the new download.
     * @param entities List of entities to be added to the download.
     * @return List of download entities
     * @throws DashboardException Caused by prepare persist.
     */
    private List<DownloadEntity> createDownloadEntities(List<Entity_> entities) throws DashboardException{
        List<DownloadEntity> collection = new ArrayList();
        
        for(Entity_ e: entities){
            DownloadEntity de = new DownloadEntity();            
            de.setDownload(download);
            de.setEntity(e);
            de.preparePersist();
            collection.add(de);
        }
        
        return collection;
    }
    /**
     * The main method that creates the download entities. Will prepare persist
     * each entity. Global variable download is assigned to each download
     * entity.
     *
     * @param entities A JSON string
     * @return A list of download entities.
     * @throws DashboardException If there is an issue creating the entities.
     */
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

    /**
     * Creates the actual entity into the dashboard database. Checks to make
     * sure the entity doesn't already exists. If it doesn't exists then it is
     * freshly created. The download size is updated on the fly as well. The
     * entities are also prepare persists as well.
     *
     * @param id The Id of the entity in the ICAT.
     * @param EntityType The type of entity e.g. investigation or datafile.
     * @return The created entity that has been created in the dashboard.
     * @throws DashboardException If there has been problems inserting the data
     * into the dashboard database.
     */
    private Entity_ createEntity(Long id, String entityType) throws DashboardException {
        Entity_ entity = new Entity_();
        switch (entityType) {
            case "investigation":
                entity = checkEntity(id, entityType);
                if (entity.getId() == null) {
                    Investigation inv = getInvestigation(id);
                    entity.setICATID(id);
                    entity.setEntityName(inv.getName());
                    entity.setICATcreationTime(inv.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(getInvSize(id));
                    downloadSize += entity.getEntitySize();
                    entity.setType(entityType);
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;

            case "dataset":
                entity = checkEntity(id, entityType);
                if (entity.getId() == null) {
                    Dataset ds = getDataset(id);
                    entity.setICATID(id);
                    entity.setEntityName(ds.getName());
                    entity.setICATcreationTime(ds.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(getDatasetSize(id));
                    downloadSize += entity.getEntitySize();
                    entity.setType(entityType);
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;

            case "datafile":
                entity = checkEntity(id, entityType);
                if (entity.getId() == null) {
                    Datafile df = getDatafile(id);
                    entity.setICATID(id);
                    entity.setEntityName(df.getName());
                    entity.setICATcreationTime(df.getCreateTime().toGregorianCalendar().getTime());
                    entity.setEntitySize(df.getFileSize());
                    downloadSize += entity.getEntitySize();
                    entity.setType(entityType);
                    entity.preparePersist();
                    beanManager.create(entity, manager);
                }
                break;
        }
        downloadSize += entity.getEntitySize();
        return entity;
    }

    /**
     * Checks to see if the entity already exists in the database.
     *
     * @param ID the ICAT ID for the entity.
     * @param type the type of entity it is e.g. investigation.
     * @return the object if it's found. If not then null is returned.
     */
    ;private Entity_ checkEntity(Long ID, String type) throws InternalException {
        List<Object> en = beanManager.search("SELECT en FROM Entity_ en WHERE en.type='" + type + "' AND en.ICATID=" + ID, manager);
        if (en.size() > 0) {
            return (Entity_) en.get(0);
        }
        return new Entity_();
    }

    /**
     * Gets an investigation object from the ICAT.
     *
     * @param id The ID of the investigation.
     * @return An investigation object from the ICAT.
     */
    public Investigation getInvestigation(Long id) {
        Investigation inv = null;

        try {
            inv = (Investigation) icat.get(sessionID, "Investigation", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return inv;
    }

    /**
     * Gets a dataset object from the ICAT.
     *
     * @param id The id of the dataset.
     * @return A dataset object from the ICAT
     */
    public Dataset getDataset(Long id) {
        Dataset ds = null;

        try {
            ds = (Dataset) icat.get(sessionID, "Dataset", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ds;
    }

    /**
     * Gets a datafile object from the ICAT.
     *
     * @param id The id of the datafile.
     * @return A datafile object from the ICAT.
     */
    public Datafile getDatafile(Long id) {
        Datafile df = null;

        try {
            df = (Datafile) icat.get(sessionID, "Datafile", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return df;

    }

    /**
     * Gets the size of the investigation by summing all of it's datasets and
     * datafiles.
     *
     * @param id The id of the investigation in ICAT.
     * @return The total size of the investigation.
     */
    public Long getInvSize(Long id) {
        long size = 0;

        try {
            size = Long.parseLong((icat.search(sessionID, "SELECT SUM(d.fileSize) FROM Datafile d JOIN d.dataset ds JOIN ds.investigation i WHERE i.id=" + id).toArray()[0].toString()));
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return size;
    }

    /**
     * *
     * Gets the size of the dataset by summing all of the datafiles.
     *
     * @param id The id of the dataset in ICAT.
     * @return The total size of the dataset.
     */
    public long getDatasetSize(Long id) {
        long size = 0;

        try {
            size = Long.parseLong(icat.search(sessionID, "SELECT SUM(d.fileSize) FROM Datafile d JOIN d.dataset ds WHERE ds.id=" + id).toArray()[0].toString());
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return size;
    }

    /**
     * Creates an ICAT object that can be used to communicate the provided ICAT.
     *
     * @return an ICAT object.
     */
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
