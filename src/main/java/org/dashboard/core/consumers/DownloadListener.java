/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.Base64;
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
import org.dashboard.core.entity.EntityCollection;
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
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;
    
    @PostConstruct
    private void init(){
        sessionID = session.getSessionID();
    }
   
    @Override
    public void onMessage(Message message) {
        Download download = new Download();
        TextMessage text = (TextMessage)message;
        
        try {
            download.setIpAddress(InetAddress.getByName(message.getStringProperty("ipAddress")));
            download.setUser(getUser(message.getStringProperty("user")));
            download.setEntityCollection(createEntityCollection(text.getText()));
        } catch (UnknownHostException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private EntityCollection createEntityCollection(String entities){
        EntityCollection collection = new EntityCollection();
        List<Entity_> entityList = null;
        Entity_ ent = new Entity_();
        
        try {
            JSONParser parser = new JSONParser();            
            Object obj = parser.parse(entities);
            JSONObject json = (JSONObject) obj;
            
            if(json.containsKey("investigationIds")){
                JSONArray dfIDs = (JSONArray) json.get("investigationIds");
                for(int i=0;i<dfIDs.size();i++){
                    int id = Integer.parseInt(dfIDs.get(i).toString());
                    ent = createEntity(id,"investigation");
                    entityList.add(ent);
                }               
            }
            if(json.containsKey("datasetIds")){
                JSONArray dfIDs = (JSONArray) json.get("datasetIds");
                for(int i=0;i<dfIDs.size();i++){
                    int id = Integer.parseInt(dfIDs.get(i).toString());
                    ent = createEntity(id,"dataset");
                    entityList.add(ent);
                }               
            }
            if(json.containsKey("datafileIds")){
                JSONArray dfIDs = (JSONArray) json.get("dataFileIds");
                for(int i=0;i<dfIDs.size();i++){
                    int id = Integer.parseInt(dfIDs.get(i).toString());
                    ent = createEntity(id,"datafile");
                    entityList.add(ent);
                }               
            }
            
            collection.setEntites(entityList);
                        
        } catch (ParseException ex) {
            Logger.getLogger(DownloadListener.class.getName()).log(Level.SEVERE, null, ex);       
        }
        
        return collection;
            
        
    }
    
    private Entity_ createEntity(int id,String EntityType){
        Entity_ entity = new Entity_();
        
        switch (EntityType) {
            case "investigation":
                Investigation inv = getInvestigation(id);
                entity.setEntityName(inv.getName());
                entity.setICATcreationTime(inv.getCreateTime().toGregorianCalendar().getTime());
                entity.setEntitySize(new Long(0));
                entity.setType("investigation");         
                break;
                
            case "dataset":
                Dataset ds = getDataset(id); 
                entity.setEntityName(ds.getName());
                entity.setICATcreationTime(ds.getCreateTime().toGregorianCalendar().getTime());
                entity.setEntitySize(getEntitySize("dataset",id));
                entity.setType("dataset");
                break;
                
            case "datafile":
                Datafile df = getDatafile(id);
                entity.setEntityName(df.getName());
                entity.setICATcreationTime(df.getCreateTime().toGregorianCalendar().getTime());
                entity.setEntitySize(df.getFileSize());
                entity.setType("datafile");                
                break;
    }
        return entity;
    }
    
    private Long getEntitySize(String type, int ID){
        
        switch(type){
            case "investigation":
                return getInvSize(ID);                
            case "dataset":
                break;
        }
        
        return new Long(0);
        
    }
    /**
     * Calls the topcat restful API for the method of download. Parses the JSON string to allow
     * access to the transport method.
     * 
     * @param preparedID the preapredID associated with the download
     * @return method of download e.g. https, globus etc...
     */
    private String getMethod(String preparedID){
        String method = null;
        try {
            URL topCatURL = new URL(prop.getTopCatURL()+api+preparedID);            
            
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
    
    /**
     * Encodes the topcat admin password and username to Base64
     * @return Base64 topcat credentials
     */
    private String getCredentials() {
        String rawUser = prop.getTopCatUser();
        String rawPass = prop.getTopCatPass();
        String rawCred = rawUser+":"+rawPass;   
        byte[] authEncBytes = Base64.getEncoder().encode(rawCred.getBytes());
        String authStringEnc = new String(authEncBytes);
   
        
    return "Basic "+authStringEnc;
  }
    
         
    /**
     * Retrieves the user from the dashboard. If the user is found in the dashboard then the user is inserted into the
     * dashboard before being returned.
     * @param name Unique name of the user in the ICAT
     * @return a dashboard User object.
     */
    public ICATUser getUser(String name){
        ICATUser dashBoardUser = new ICATUser();
        log.info("Searching for user: "+name+" in dashboard.");
        List<Object> user = beanManager.search("SELECT u FROM USER u WHERE u.name= "+name+"'", manager);        
        
        if(user.get(0)==null){
            log.info("No user found in the Dashboard. Retrieving from ICAT. ");  
            dashBoardUser = userCollector.insertUser(name);          
            
        }
        else{
            log.info("Found the user in the dashboard");
            dashBoardUser = (ICATUser)user.get(0);
        }
        
        return dashBoardUser;        
    }
    
     public Investigation getInvestigation(int id){
        Investigation inv = null;
        
        try {
             inv = (Investigation) icat.get(sessionID, "investigation", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inv;
    }
    
    public Dataset getDataset(int id){
        Dataset ds = null;
        
        try {
             ds = (Dataset) icat.get(sessionID, "dataset", id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ds;
    }
    
    public Datafile getDatafile(int id){
        Datafile df = null;
        
        try{
            df = (Datafile) icat.get(sessionID, "datafile",id);
        }catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return df;
        
            
    }
    
    public Long getInvSize(int id){
        List<Object> s = null;
        try {
            s = icat.search(sessionID, "SELECT SUM(d.fileSize) FROM Datafile d JOIN d.dataset ds JOIN ds.investigation i WHERE i.id="+id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        Long size = (Long) s.get(0);
        
        return size;
    }
    
    public Long getDatasetSize(int id){
        List<Object> s = null;
        try {
            s = icat.search(sessionID, "SELECT SUM(d.fileSize) FROM Datafile d JOIN d.dataset ds WHERE ds.id="+id);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
        Long size = (Long) s.get(0);
        
        return size;
    }
    
     public ICAT createICATLink(){
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
