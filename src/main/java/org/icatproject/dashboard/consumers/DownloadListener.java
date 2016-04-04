/**
 * *
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 **
 */
package org.icatproject.dashboard.consumers;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.dashboard.collector.UserCollector;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.DownloadEntity;
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
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.manager.DashboardSessionManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;


@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName="maxSessions",propertyValue="1"),
    @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "jms/IDS/log"),
    @ActivationConfigProperty(propertyName= "destination", propertyValue="jms_IDS_log"),
    @ActivationConfigProperty(propertyName="acknowledgeMode", propertyValue="Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability",propertyValue = "Durable"),   
    @ActivationConfigProperty(propertyName = "clientId",propertyValue = "dashboardID921"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "dashboardSub"),
    
    
    
})



/**
 * DownloadListener is a Message driven bean that processes JMS messages from an IDS.
 * It deals with two types of messages the getData call and the prepareData call. 
 * The class deals with these messages by extracting all of the data from the JMS text body
 * and properties. It will then collect extra information from the ICAT and then from TopCat. 
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

    private String topCatApi; 
    
    //Constants for download statuses,
    
    private final String preparing = "preparing";
    private final String inProgress = "inProgress";
    private final String finished = "finished";
    private final String failed = "failed";

      

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
        topCatApi= prop.getTopCatURL()+"/api/v1/admin/downloads?icatUrl="+prop.getICATUrl()+"&sessionId="+sessionManager.getSessionID()+"&queryOffset=";
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
                case "getDataStart":
                    getDataStartHandler(text);
                    break;
            }

        } catch (JMSException | ParseException ex) {
            logger.error("An error has occured", ex);
        }
    }

    /**
     * 
     * Deals with prepareData messages. Will create the majority of
     * the download information. 
     *
     * @param message The message from JMS.
     */
    private void prepareDataHandler(TextMessage message) throws ParseException {
        try {
           
            HashMap<String,Object> messageValues = parseJMSText(message.getText());            
            download = new Download();
            download.setUser(getUser(messageValues.get("userName").toString()));
            download.setDownloadEntities(createDownloadEntities(messageValues));
            download.setDownloadSize(downloadSize);
            download.setMethod(getMethod(messageValues.get("preparedId").toString()));
            download.setPreparedID(messageValues.get("preparedId").toString());
            download.setLocation(getLocation(message.getStringProperty("ip")));   
            download.setDownloadEntityAges(createDownloadEntityAges(messageValues));            
            download.setStatus(preparing);
            beanManager.create(download, manager);
            

        } catch (IcatException_Exception| JMSException | DashboardException | SecurityException | IllegalStateException ex) {
            logger.error("A Fatal Error has Occured ",ex);
        }
    }
    
    private void getDataHandler(TextMessage message) throws JMSException{
        
        HashMap<String,Object> messageValues = parseJMSText(message.getText());
        
        long duration = message.getLongProperty("millis");   
        long startMilli = message.getLongProperty("start");
        
        Date endDate = new Date(startMilli+duration);
        
         
        try {
            download = getDownload((Long)messageValues.get("transferId"));
            download.setDownloadEnd(endDate);
           
            download.setDuriation(duration);
           
            if(messageValues.containsKey("exceptionClass")){
                download.setStatus(failed);
            }
            else{
                 download.setStatus(finished);
                 //Don't want to set the bandiwdth if it failed as do not know how much was downloaded.
                 download.setBandwidth(calculateBandwidth(duration ,download.getDownloadSize()));
            }
               
           
            beanManager.update(download, manager);
            
            
        } catch (InternalException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }
         
                 
         
        
    }

    /**
     * Either creates or updates the download depending on if it is a standard getData call
     * or a getData with a preparedId call.
     *
     * @param message The JMS message that contains the download information.
     */
    private void getDataStartHandler(TextMessage message)  {
        try {
         
            
            HashMap<String,Object> messageValues = parseJMSText(message.getText());   
            
            if(messageValues.containsKey("preparedId")){
                checkDownload(message);                
            }else{
                createDownload(message);
            }            
           
            
        } catch (DashboardException | JMSException |  ParseException | InterruptedException | IcatException_Exception ex) {
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
    private void checkDownload(TextMessage message) throws JMSException, ParseException, DashboardException, IcatException_Exception, InterruptedException {
        
        HashMap<String,Object> messageValues = parseJMSText(message.getText());   
       
        download = getDownload(messageValues.get("preparedId").toString());       
        
        long startMilli = message.getLongProperty("start");
        
        //Has happened before so requires a new download.
        if(download.getDownloadEnd()!=null){
            String ipAddress = message.getStringProperty("ip");
            createDownload(download, ipAddress, startMilli, (Long)messageValues.get("transferId"));          
        }
        else {
            //Update download                
            
            download.setDownloadStart(new Date(startMilli));                      
            download.setStatus(inProgress);
            download.setTransferID((Long)messageValues.get("transferId"));
            beanManager.update(download, manager);
        }
        
    }

    /**
     * Creates the download for a getData call without a preparedID.
     * @param message the getData JMS message.
     */
    private void createDownload(TextMessage message) throws InternalException, IcatException_Exception{
        try {            
            
            long startMilli = message.getLongProperty("start");
            
            HashMap<String,Object> messageValues = parseJMSText(message.getText());
            
            download = new Download();
            download.setUser(getUser(messageValues.get("userName").toString()));
            download.setDownloadEntities(createDownloadEntities(messageValues));
            download.setDownloadSize(downloadSize);            
            download.setLocation(getLocation(message.getStringProperty("ip")));
            //Assumes the user is using a secure download.
            download.setMethod("https");           
            download.setDownloadStart(new Date(startMilli));
            download.setDownloadEntityAges(createDownloadEntityAges(messageValues));
            download.setTransferID((Long)messageValues.get("transferId"));     
            download.setStatus(inProgress);
            beanManager.create(download, manager);
        } catch (DashboardException | JMSException | ParseException ex) {
            logger.error("A Fatal Error has Occured ",ex);
        }
        
                
    }
    
    /**
     * Parses the message body to retrieve all of the relevant information.
     * @param message from the JMS.
     * @return List of data retrieved from the JMS Message.
     */
    private HashMap<String,Object> parseJMSText(String messageBody){
        
        HashMap<String,Object> messageValues = new HashMap<>();
        
        
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(messageBody);
            JSONObject json = (JSONObject) obj;
            
            if(json.containsKey("userName")){
                messageValues.put("userName",(String)json.get("userName"));            
            }
            
            if(json.containsKey(("preparedId"))){
                messageValues.put("preparedId",(String)json.get("preparedId"));
            }
            
            if(json.containsKey(("investigationIds"))){
                JSONArray investigationIds = (JSONArray)json.get("investigationIds");
                messageValues.put("investigationIds", investigationIds);

            }
            
            if(json.containsKey("datasetIds")){
                 JSONArray datasetIds = (JSONArray)json.get("datasetIds");
                messageValues.put("datasetIds", datasetIds);
            }
            
            if(json.containsKey("datafileIds")){
                 JSONArray datafileIds = (JSONArray)json.get("datafileIds");
                messageValues.put("datafileIds", datafileIds);
            }
            
            if(json.containsKey("transferId")){
                 messageValues.put("transferId",(Long)json.get("transferId"));
            }
            
            if(json.containsKey("exceptionClass")){
                messageValues.put("exceptionClass",(String)json.get("exceptionClass"));
            }
            
        } catch (ParseException ex) {
            logger.info("Issue with parsing the JMS message body: ",ex.getMessage());
        }
        
        return messageValues;
        
    }
    /**
     * Overloaded method to handle a user downloading the same download order multiple
     * times.
     * @param oldDownload A download that contains the same preparedID which will contain the same entities.
     * @param ipAddress  of the new download.
     * @param duration of the the new download.
     * @param startMilli the start time in milliseconds.
     */
    private void createDownload(Download oldDownload, String ipAddress, long startMilli, long transferId) throws DashboardException, ParseException, IcatException_Exception, JMSException{
        
        download = new Download();        
        download.setUser(oldDownload.getUser());
        download.setPreparedID(oldDownload.getPreparedID());
        download.setDownloadEntities(createDownloadEntities(getEntities(oldDownload.getId())));
        download.setDownloadSize(oldDownload.getDownloadSize());            
        download.setLocation(getLocation(ipAddress));
        download.setMethod(oldDownload.getMethod());        
        download.setDownloadStart(new Date(startMilli));
        download.setDownloadEntityAges(createDownloadEntityAges(getDownloadEntityIDs(oldDownload.getPreparedID()))); 
        download.setTransferID(transferId);
        download.setStatus(inProgress);
        beanManager.create(download, manager);
        
        
    }

 

    /**
     * Creates a download location object using the GeoTool module.
     *
     * @param ipAddress The idAddress to have its GeoLocation resolved.
     */
    private GeoLocation getLocation(String ipAddress) {
        GeoLocation location = GeoTool.getGeoLocation(ipAddress, manager, beanManager);      

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
        
         double bandwidth = (double) size / ((double) duration/1000);
        
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
    
    /**
     * Overloaded method that gets a download from it's transfer ID and status.
     * @param transferId The id used to identify a download.
     * @return The download requested. 
     */
    private Download getDownload(Long transferId) throws InternalException{
        List<Object> existingDownload = beanManager.search("SELECT d FROM Download d WHERE d.transferID='" + transferId + "' AND d.status='"+inProgress+"'", manager);
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
    private String getMethod(String preparedID)  {
       
        String method = callTopCat(preparedID).get("transport").toString();       
        return method;
         
    }
    
    /**
     * Contacts the topCat and returns all the download data associated with that
     * preparedID.
     * @param prepearedID of the download.
     * @return A JSONObject of the topCat response.
     */
    private JSONObject callTopCat(String preparedID){
        JSONObject topCatOutput = null;
        
        try {
            Client client = Client.create();
            String url =  URLEncoder.encode("where download.preparedId='"+preparedID+"'", "UTF-8");
            WebResource webResource  = client.resource(topCatApi+url);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
            String result = response.getEntity(String.class);
            
            
            JSONParser parser = new JSONParser();

            Object obj = parser.parse(result);
            JSONArray jsonArray = (JSONArray) obj;            
            topCatOutput = (JSONObject) jsonArray.get(0);
        }
        catch (IOException | ParseException ex) {
             logger.error("A Fatal Error has Occured ",ex);
        }
        
        return topCatOutput;
                
        
    }
    
    /**
     * Method that calls the TopCat API to retrieve the entity IDs associated with
     * the preparedID.
     * @param preparedID Unique download Identifier.
     * @return JSON String that contains all of the entities and what entity they are 
     * associated with.
     * @throws ParseException issues accessing the returned JSON string. 
     */    
    private HashMap getDownloadEntityIDs(String preparedID) throws ParseException{
        
        String entityIDs = callTopCat(preparedID).get("downloadItems").toString();       

        return formatTopCatOutput(entityIDs);
        
    }

    /***
     * Formats the topcat returned output to match that of the IDS JMS message text body.
     * @param topCatOutput The ouput returned from topcat.
     * @return A JSON String containing an array of entities associated with what
     * entity they are.
     * @throws ParseException issue accessing the the topCatOutput. 
     */
    private HashMap formatTopCatOutput(String topCatOutput) throws ParseException{
        
        HashMap<String,Object> entityIds = new HashMap<>();
        
        
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
                       
           entityIds.put("investigationIds",investigationIDs);
        }
        if(datasetIDs.size()>0){
            entityIds.put("datasetIds", datasetIDs);
        }
        if(datafileIDs.size()>0){
           entityIds.put("datafileIds", datafileIDs);
        }
        
        
        
        return entityIds;
        
        
    }
    
  
   
    /**
     * Gets the user from the dashboard database. If the user isn't found then
     * they are inserted into the dashboard.
     *
     * @param name the name of the user in the ICAT.
     * @return the dashboard user with the provided name.
     * @throws ParseException issue accessing the string messageBody.
     */
    public ICATUser getUser(String name) throws ParseException {
       
        ICATUser dashBoardUser;
        
        logger.info("Searching for user: " + name + " in dashboard.");
        List<Object> user = null;
        try {
            user = beanManager.search("SELECT u FROM ICATUser u WHERE u.name='" + name + "'", manager);
        } catch (InternalException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (user.size()==0) {
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
    private List<DownloadEntityAge> createDownloadEntityAges(HashMap messageValues) throws DashboardException, ParseException, IcatException_Exception {
        
        List<DownloadEntityAge> collection = new ArrayList();       
       
        List<Object> dates = new ArrayList();       
        

        if (messageValues.containsKey("investigationIds")) {
            JSONArray invIDs = (JSONArray) messageValues.get("investigationIds");
            for (int i = 0; i < invIDs.size(); i++) {
                dates.addAll(getInvestigationEntityAges(invIDs.get(i).toString()));               

            }
        }
        if (messageValues.containsKey("datasetIds")) {
            JSONArray dsIDs = (JSONArray) messageValues.get("datasetIds");
            for (int i = 0; i < dsIDs.size(); i++) {
                dates.addAll(getDatasetEntityAges(dsIDs.get(i).toString()));        

            }
        }
        if (messageValues.containsKey("datafileIds")) {
            JSONArray dfIDs = (JSONArray) messageValues.get("datafileIds");
            for (int i = 0; i < dfIDs.size(); i++) {
                dates.addAll(getDataFileEntityAge(dfIDs.get(i).toString()));        
                
            }
        }
        
        Map<Long,Long> entityAgeMap = createEntityAgeMap(dates);
        
        for(Map.Entry<Long,Long> entry : entityAgeMap.entrySet()){
            DownloadEntityAge dea = new DownloadEntityAge();
            
            long amount = entry.getValue();
            long age = entry.getKey();
            
            dea.setAge(age);
            dea.setAmount(amount);
            
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
        
        dates =  icat.search(sessionID, "SELECT df.datafileCreateTime FROM Datafile df JOIN df.dataset ds WHERE ds.id='"+datasetID+"'");
        
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
    private List<DownloadEntity> createDownloadEntities(HashMap messageValues) throws DashboardException {
        List<DownloadEntity> collection = new ArrayList();
        Entity_ ent;

        if (messageValues.containsKey("investigationIds")) {
             JSONArray invIDs  = (JSONArray) messageValues.get("investigationIds");
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
        if (messageValues.containsKey("datasetIds")) {
            JSONArray dsIDs = (JSONArray) messageValues.get("datasetIds");
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
        if (messageValues.containsKey("datafileIds")) {
            JSONArray dfIDs = (JSONArray) messageValues.get("datafileIds");
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
        //Check if entity already exists. 
        Entity_ entity = checkEntity(id, entityType);
        
        //If not found then create a new one.
        if(entity.getId()==null){
            if("investigation".equals(entityType)){        

                Investigation inv = getInvestigation(id);
                entity.setIcatId(id);
                entity.setEntityName(inv.getName());
                entity.setICATcreationTime(inv.getCreateTime().toGregorianCalendar().getTime());
                entity.setEntitySize(getInvSize(id));                   
                            }
            else if("dataset".equals(entityType)){           

                Dataset ds = getDataset(id);
                entity.setIcatId(id);
                entity.setEntityName(ds.getName());
                entity.setICATcreationTime(ds.getCreateTime().toGregorianCalendar().getTime());
                entity.setEntitySize(getDatasetSize(id));                   

            }
            else if("datafile".equals(entityType)){

                Datafile df = getDatafile(id);
                entity.setIcatId(id);
                entity.setEntityName(df.getName());
                entity.setICATcreationTime(df.getCreateTime().toGregorianCalendar().getTime());
                entity.setEntitySize(df.getFileSize());                    


             }                

            entity.setType(entityType);
            entity.preparePersist();
            
            beanManager.create(entity, manager);
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

    
}
