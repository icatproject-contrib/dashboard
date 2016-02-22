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
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
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
import org.icatproject.dashboard.entity.DownloadEntityAge;
import org.icatproject.dashboard.manager.DashboardSessionManager;
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
    @ActivationConfigProperty(propertyName = "clientId",propertyValue = "dashboardID4"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "dashboardSub"),
    
})


/**
 * DownloadListener is a Message driven bean that processes JMS messages from an IDS.
 * It deals with two types of messages the getData call and the prepareData call. 
 * The class deals with these messages by extracting all of the data from the JMS text body
 * and properties. It will then collect extra information from the ICAT and from the TopCat. 
 * With all this information it then pushes the data to the database.
 */
public class DownloadListener implements MessageListener {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DashboardSessionManager.class);

    @EJB
    private PropsManager prop;

    @EJB
    private ICATSessionManager sessionManager;

    @EJB
    private EntityBeanManager beanManager;

    @EJB
    private UserCollector userCollector;

    protected ICAT icat;

    protected String sessionID;

    private final String api = "/api/v1/admin/downloads?preparedId=";

      

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
            logger.debug("An error has occured", ex);
        }
    }

    /**
     * *
     * Deals with prepareData messages. Will create the majority of
     * the download information. 
     *
     * @param message The message from JMS.
     */
    private void prepareDataHandler(TextMessage message) throws ParseException {
        try {
           
            String preparedId = getPreparedId(message.getText());
            download = new Download();
            download.setUser(getUser(message.getText()));
            download.setDownloadEntities(createDownloadEntities(message.getText()));
            download.setDownloadSize(downloadSize);
            download.setMethod(getMethod(preparedId));
            download.setPreparedID(preparedId);
            download.setLocation(getLocation(message.getStringProperty("ip")));            
            beanManager.create(download, manager);
            

        } catch (JMSException | DashboardException | SecurityException | IllegalStateException ex) {
            logger.error("A Fatal Error has Occured ",ex);
        }
    }

    /**
     * Either creates or updates the download depending on if it is a standard getData call
     * or a getData with a preparedId call.
     *
     * @param message The JMS message that contains the download information.
     */
    private void getDataHandler(TextMessage message)  {
        try {
         
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(message.getText());
            JSONObject json = (JSONObject) obj;
            
            if(json.containsKey("preparedId")){
                checkDownload(message);                
            }else{
                createDownload(message);
            }            
           
            
        } catch (DashboardException | JMSException |  ParseException  | IcatException_Exception ex) {
             logger.error("A Fatal Error has Occured",ex);
        }

    }
    
    /**
     * Checks the download to see if it either needs updating or a new download
     * needs to be created. 
     * @param message The JMS Message   
     * @throws JMSException If there is an issue with accessing the JMS message properties.
     * @throws ParseException if there is an issue parsing the JSON message.
     */
    private void checkDownload(TextMessage message) throws JMSException, ParseException, DashboardException, IcatException_Exception {
        
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
            //Update download
                 
            download.setDownloadEnd(new Date(startMilli+duration));
            download.setDownloadStart(new Date(startMilli));
            download.setDuriation(duration);
            download.setDownloadEntityAges(createDownloadEntityAges(getDownloadEntityIDs(download.getPreparedID())));
            download.setBandwidth(calculateBandwidth(duration ,download.getDownloadSize()));

            beanManager.update(download, manager);
        }
        
    }

    /**
     * Creates the download for a getData call without a preparedID.
     * @param message the getData JMS message.
     */
    private void createDownload(TextMessage message) throws InternalException, IcatException_Exception{
        try {            
            long duration = message.getLongProperty("millis");
            long startMilli = message.getLongProperty("start");
            
            
            download = new Download();
            download.setUser(getUser(message.getText()));
            download.setDownloadEntities(createDownloadEntities(message.getText()));
            download.setDownloadSize(downloadSize);            
            download.setLocation(getLocation(message.getStringProperty("ip")));
            //Assumes the user is using a secure download.
            download.setMethod("https");
            download.setDuriation(duration);
            download.setDownloadEnd(new Date(startMilli+duration));
            download.setDownloadStart(new Date(startMilli));
            download.setDownloadEntityAges(createDownloadEntityAges(message.getText()));
            download.setBandwidth(calculateBandwidth(download.getDuriation(),download.getDownloadSize()));
            beanManager.create(download, manager);
        } catch (DashboardException | JMSException | ParseException ex) {
            logger.error("A Fatal Error has Occured ",ex);
        }
        
                
    }
    /**
     * Overloaded method to handle a user downloading the same download order multiple
     * times.
     * @param oldDownload A download that contains the same preparedID which will contain the same entities.
     * @param ipAddress  of the new download.
     * @param duration of the the new download.
     * @param startMilli the start time in milliseconds.
     */
    private void createDownload(Download oldDownload, String ipAddress, long duration, long startMilli) throws DashboardException, ParseException, IcatException_Exception, JMSException{
        
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
        download.setDownloadEntityAges(createDownloadEntityAges(getDownloadEntityIDs(oldDownload.getPreparedID())));
        download.setBandwidth(calculateBandwidth(oldDownload.getDuriation(),oldDownload.getDownloadSize()));
        beanManager.create(download, manager);
        
        
    }

    /**
     * Retrieves the preparedId from the message body.
     *
     * @param messageBody The JMS message body
     * @return the PreparedId from the JMS message.
     */
    private String getPreparedId(String messageBody)  {
        String preparedId = null;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(messageBody);
            JSONObject json = (JSONObject) obj;
            
            preparedId = (String) json.get("preparedId");
            
            
        } catch (ParseException ex) {
            logger.error("A Fatal Error has Occured ",ex);
        }
        
        return preparedId;
    }

    /**
     * Creates a download location object using the GeoTool module.
     *
     * @param ipAddress The idAddress to have its GeoLocation resolved.
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
     * Retrieves the download from the dashboard database.
     *
     * @param preparedID The unique identifier of the download.
     * @return The download object requested.
     */
    private Download getDownload(String preparedID) throws InternalException {
        List<Object> existingDownload = beanManager.search("SELECT d FROM Download d WHERE d.preparedID='" + preparedID + "'", manager);
        if (existingDownload.size() > 0) {
            return (Download) existingDownload.get(0);
        }
        //not found rare case
        return null;

    }
    
    /***
     * Gathers the Entities from a pre existing download and gathers them into a list. 
     * @param downloadID of the download you wish to get the entities from. 
     * @return a list of Entity_ objects that are associated with a download.
     */
    private List<Entity_> getEntities(long downloadID) {
        List<Object> entityQuery = new ArrayList();
        List<Entity_> entities = new ArrayList();
        
        try {        
            entityQuery = beanManager.search("SELECT e FROM Entity_ e JOIN e.downloadEntities ed JOIN ed.download d WHERE d.id='"+ downloadID +"'", manager);
        } catch (InternalException ex) {
            logger.error("A Fatal Error has Occured ",ex);
        }
        
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
            StringBuilder buffer = new StringBuilder();

            while ((output = responseBuffer.readLine()) != null) {
                buffer.append(output);
            }

            JSONParser parser = new JSONParser();

            Object obj = parser.parse(buffer.toString());
            JSONArray jsonArray = (JSONArray) obj;
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            method = jsonObject.get("transport").toString();

        } catch (IOException | ParseException ex) {
             logger.error("A Fatal Error has Occured ",ex);
        }
        return method;
         
    }
    
    /**
     * Method that calls the TopCat API to retrieve the entity IDs associated with
     * the preparedID.
     * @param preparedID Unique download Identifier.
     * @return JSON String that contains all of the entities and what entity they are 
     * associated with.
     * @throws ParseException issues accessing the returned JSON string. 
     */    
    private String getDownloadEntityIDs(String preparedID) throws ParseException{
        
        String entityIDs = null;
        
        try {
            URL topCatURL = new URL(prop.getTopCatURL() + api + preparedID);
            HttpsURLConnection httpsConnection = (HttpsURLConnection) topCatURL.openConnection();
            httpsConnection.setRequestMethod("GET");
            httpsConnection.setRequestProperty("Accept", "application/json");
            httpsConnection.addRequestProperty("Authorization", getCredentials());

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpsConnection.getInputStream())));

            String output;
            StringBuilder buffer = new StringBuilder();

            while ((output = responseBuffer.readLine()) != null) {
                buffer.append(output);
            }

            JSONParser parser = new JSONParser();

            Object obj = parser.parse(buffer.toString());
            JSONArray jsonArray = (JSONArray) obj;
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            entityIDs = jsonObject.get("downloadItems").toString();

        } catch (IOException | ParseException ex) {
             logger.error("A Fatal Error has Occured ",ex);
        }

        return formatTopCatOutput(entityIDs);
        
    }

    /***
     * Formats the topcat returned output to match that of the IDS JMS message text body.
     * @param topCatOutput The ouput returned from topcat.
     * @return A JSON String containing an array of entities associated with what
     * entity they are.
     * @throws ParseException issue accessing the the topCatOutput. 
     */
    private String formatTopCatOutput(String topCatOutput) throws ParseException{
        
        JSONObject formatted = new JSONObject();
        
        JSONParser parser = new JSONParser();        
        Object obj = parser.parse(topCatOutput);
        JSONArray array = (JSONArray) obj;

        JSONArray investigationIDs = new JSONArray();
        JSONArray datasetIDs = new JSONArray();
        JSONArray datafileIDs = new JSONArray();
        
       
        
        for(int i=0; i<array.size();i++){
            JSONObject temp = (JSONObject) array.get(i);
            String type = (String) temp.get("entityType");
            long id = (long) temp.get("entityId");
            
            if("investigation".equals(type)){
                investigationIDs.add(id);
            }
            else if("dataset".equals(type)){
                datasetIDs.add(id);
            }
            else if("datafile".equals(type)){
                 datafileIDs.add(id);
            }     
            
        }
        
        if(investigationIDs.size()>0){
                       
            formatted.put("investigationIds",investigationIDs);
        }
        if(datasetIDs.size()>0){
            formatted.put("datasetIds", datasetIDs);
        }
        if(datafileIDs.size()>0){
            formatted.put("datafileIds", datafileIDs);
        }
        
        
        
        return formatted.toJSONString();
        
        
    }
    
  
   
    /**
     * Gets the user from the dashboard database. If the user isn't found then
     * they are inserted into the dashboard.
     *
     * @param messageBody the message body of the JMS message
     * @return the dashboard user with the provided name.
     * @throws ParseException issue accessing the string messageBody.
     */
    public ICATUser getUser(String messageBody) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(messageBody);
        JSONObject json = (JSONObject) obj;

        String name = (String) json.get("userName");

        ICATUser dashBoardUser;
        logger.info("Searching for user: " + name + " in dashboard.");
        List<Object> user = null;
        try {
            user = beanManager.search("SELECT u FROM ICATUser u WHERE u.name='" + name + "'", manager);
        } catch (InternalException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (user.get(0)!=null) {
            logger.info("No user found in the Dashboard. Retrieving from ICAT.");
            dashBoardUser = userCollector.insertUser(name);

        } else {
            logger.info("Found the user in the dashboard");
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
    
    /***
     * 
     * @param entities A JSON string of entities.
     * @return a List of DownloadEntityAge Objects
     * @throws DashboardException Problems persisting the object.
     * @throws ParseException Problems parsing the entities string.
     * @throws IcatException_Exception Problems getting the datafilecreatime from the ICAT
     */
    private List<DownloadEntityAge> createDownloadEntityAges(String entities) throws DashboardException, ParseException, IcatException_Exception {
        
        List<DownloadEntityAge> collection = new ArrayList();       
       
        List<Object> dates = new ArrayList();
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(entities);
        JSONObject json = (JSONObject) obj;

        if (json.containsKey("investigationIds")) {
            JSONArray invIDs = (JSONArray) json.get("investigationIds");
            for (int i = 0; i < invIDs.size(); i++) {
                dates.addAll(getInvestigationEntityAges(invIDs.get(i).toString()));               

            }
        }
        if (json.containsKey("datasetIds")) {
            JSONArray dsIDs = (JSONArray) json.get("datasetIds");
            for (int i = 0; i < dsIDs.size(); i++) {
                dates.addAll(getDatasetEntityAges(dsIDs.get(i).toString()));        

            }
        }
        if (json.containsKey("datafileIds")) {
            JSONArray dfIDs = (JSONArray) json.get("datafileIds");
            for (int i = 0; i < dfIDs.size(); i++) {
                dates.addAll(getDataFileEntityAge(dfIDs.get(i).toString()));        
                
            }
        }
        
        Map<Long,Long> entityAgeMap = createEntityAgeMap(dates);
        
        for(Map.Entry<Long,Long> entry : entityAgeMap.entrySet()){
            DownloadEntityAge dea = new DownloadEntityAge();
            
            dea.setAge(entry.getKey());
            dea.setAmount(entry.getValue());
            
            dea.preparePersist();
            dea.setDownload(download);
            collection.add(dea);
        }
        
        
        
        
        return collection;
    }
    
    /***
     * Gets the datafilecreatime of all datafiles in the set investigation.
     * @param investigationID The id of the investigation to search for.
     * @return A list of dates.
     * @throws IcatException_Exception Issues with getting the data from the ICAT. 
     */
    private List<Object> getInvestigationEntityAges(String investigationID) throws IcatException_Exception{
        
        List<Object> dates;
        
        dates = icat.search(sessionID, "SELECT df.datafileCreateTime FROM Datafile df JOIN df.dataset ds JOIN ds.investigation inv WHERE inv.id='"+investigationID+"'");
        
        return dates;
            
        
        
    }
    
     /***
     * Gets the datafilecreatime of all datafiles in the set dataset.
     * @param datasetID The id of the dataset to search for.
     * @return A list of dates.
     * @throws IcatException_Exception Issues with getting the data from the ICAT. 
     */
    
    private List<Object> getDatasetEntityAges(String datasetID) throws IcatException_Exception{
        
        List<Object> dates;
        
        dates =  icat.search(sessionID, "SELECT df.datafileCreateTime FROM Datafile df JOIN df.dataset WHERE ds.id='"+datasetID+"'");
        
        return dates;
        
    }
    
     /***
     * Gets the datafilecreatime of the datafile.
     * @param datafileID The id of the datafile to search for.
     * @return A single date.
     * @throws IcatException_Exception Issues with getting the data from the ICAT. 
     */
    
    private List<Object> getDataFileEntityAge(String datafileID) throws IcatException_Exception{
        
        List<Object> dates;
        
        dates =  icat.search(sessionID, "SELECT df.datafileCreateTime FROM Datafile df WHERE df.id='"+datafileID+"'");
        
        return dates;
        
    }
    
    /***
     * Processes a list of dates. Each date is subtracted from the current date to get an age in days.
     * 
     * @param dates a list of dates to calculate ages from.
     * @return a hashmap of <age of file in days, number of files of that age>
     */
    private Map<Long,Long> createEntityAgeMap(List<Object> dates){
        
        Map<Long,Long> entityAgeMap = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        
        for(Object dat: dates){
            LocalDate temp = (((XMLGregorianCalendar) dat).toGregorianCalendar().toZonedDateTime().toLocalDate());
            
            long diff = ChronoUnit.DAYS.between(temp, now);
            
            Long number = entityAgeMap.get(diff);
            
            if(number == null){
                number = new Long(1);
                entityAgeMap.put(diff, number);
            }
            else{
                number+=1;
                entityAgeMap.put(diff,number);
            }
        }
        
        return entityAgeMap;
            
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
        Entity_ ent;

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
            logger.error("Found the user in the dashboard", ex);
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
        Entity_ entity = checkEntity(id, entityType);
        
        if("investigation".equals(entityType)){        
               
            Investigation inv = getInvestigation(id);
            entity.setICATID(id);
            entity.setEntityName(inv.getName());
            entity.setICATcreationTime(inv.getCreateTime().toGregorianCalendar().getTime());
            entity.setEntitySize(getInvSize(id));                   
                        }
        else if("dataset".equals(entityType)){           
                
            Dataset ds = getDataset(id);
            entity.setICATID(id);
            entity.setEntityName(ds.getName());
            entity.setICATcreationTime(ds.getCreateTime().toGregorianCalendar().getTime());
            entity.setEntitySize(getDatasetSize(id));                   
                
        }
        else if("datafile".equals(entityType)){
            
            Datafile df = getDatafile(id);
            entity.setICATID(id);
            entity.setEntityName(df.getName());
            entity.setICATcreationTime(df.getCreateTime().toGregorianCalendar().getTime());
            entity.setEntitySize(df.getFileSize());                    
                    
                    
         }                
        
        entity.setType(entityType);
        entity.preparePersist();
        downloadSize += entity.getEntitySize();
        beanManager.create(entity, manager);
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
            logger.error("A fatal error has occured ",ex);
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
            logger.error("A fatal error has occured ",ex);
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
            logger.error("A fatal error has occured ",ex);
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
            logger.error("A fatal error has occured ",ex);
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
            logger.error("A fatal error has occured ",ex);
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
           logger.error("A fatal error has occured ",ex);
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
