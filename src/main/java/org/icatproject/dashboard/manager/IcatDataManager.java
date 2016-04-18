/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.xml.namespace.QName;

import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.icatproject.IcatException_Exception;
import org.icatproject.Login;
import org.icatproject.dashboard.exceptions.InternalException;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
@Startup
@DependsOn("PropsManager")
public class IcatDataManager {    
    
    @EJB 
    private PropsManager properties;  
    
    private static final Logger LOG = LoggerFactory.getLogger(IcatDataManager.class);  
  
    private String sessionID;
    private Session restSession;
    private ICAT icat;   
    private String authenticators;
    
            
    @Resource
    private TimerService timerService;
      
   
    /**
     * Initialises the timers and the ICAT login.
     */
    @PostConstruct
    public void init(){
        LOG.info("Initiating ICATSession Manager");
        createTimers();
        sessionID = loginICAT(properties);
        restSession = createRestSession(properties);
        authenticators = retrieveAuthenticators();
        
       
    }
     /**
     * Creates the timers with the TimerService object. Currently creates two.
     * One for the ICAT refresh with is invoked every hour and datacollect which
     * is invoked depending on the users set value.
     * 
     * @param properties The properties manager object that contains all the information
     * from the dashboard.properties file.
     */
    private void createTimers(){
        
        TimerConfig refreshSession = new TimerConfig("refreshSession", false);
        TimerConfig refreshAuthenticators = new TimerConfig("refreshAuthenticators", false);
        
        timerService.createIntervalTimer(1200000,1200000,refreshAuthenticators);        
        timerService.createIntervalTimer(3600000,3600000,refreshSession);         
       
        
    }
    
    private String retrieveAuthenticators(){
        URL url;
        JSONArray mnemonicArray = new JSONArray();

        try {
            url = new URL(properties.getICATUrl() + "/icat/properties");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder buffer = new StringBuilder();
            String output;

            while ((output = responseBuffer.readLine()) != null) {
                buffer.append(output);
            }

            conn.disconnect();

            JSONParser parser = new JSONParser();

            JSONObject response = (JSONObject) parser.parse(buffer.toString());
            JSONArray authns = (JSONArray) response.get("authenticators");

            mnemonicArray = new JSONArray();

            for (int i = 0; i < authns.size(); i++) {
                JSONObject temp = (JSONObject) authns.get(i);
                JSONObject mnemonic = new JSONObject();

                mnemonic.put("mnemonic", temp.get("mnemonic"));

                mnemonicArray.add(mnemonic);
            }
        } catch (IOException | ParseException ex) {
                LOG.error("Unable to collect the authenticator list from the ICAT ",ex);
        }

        return mnemonicArray.toJSONString();
    }
   /**
     * Logs into the ICAT
     * @param icatURL The URL of the ICAT
     * @param userName user name for the ICAT login
     * @param password password for the ICAT login
     * @param authenticator type of authenticator
     * @return 
     */
    private String loginICAT(PropsManager properties){
        
        String session =null;
        try {
            URL hostUrl;
            
            hostUrl = new URL(properties.getICATUrl());
            URL icatUrl = new URL(hostUrl, "ICATService/ICAT?wsdl");
            QName qName = new QName("http://icatproject.org", "ICATService");
            ICATService service = new ICATService(icatUrl, qName);
            icat = service.getICATPort();            
                                        
                    
        } catch (MalformedURLException ex) {
            LOG.error("Error connecting to the ICAT ",ex);
        }
        
       
        
        try {
            session = icat.login(properties.getAuthenticator(), getCredentials(properties.getReaderUserName(),properties.getReaderPassword()));
            LOG.info("Successfully Logged into ICAT");
        } catch (IcatException_Exception ex) {
            LOG.error("Error logging into the ICAT ",ex);
        }
        
        return session;
        
    }
    
     /**
     * Handles the timers. If statement inside decides what timer was called and what
     * method should be invoked to deal with that timer.
     * Currently only refresh session and collection of data.
     * @param timer is the object that is invoked when the timerservice is invoked.
     */
    @Timeout
    public void timeout(Timer timer){
        if("refreshSession".equals(timer.getInfo())){
            refreshSession();
        }
        else if("refreshAuthenticators".equals(timer.getInfo())){
            authenticators = retrieveAuthenticators();
        }
        
    }
    
    /**
     * Creates an ICAT RestFul client.
     * @param properties that contain the login details.
     * @return a ICAT RestFul client.
     */
    private Session createRestSession(PropsManager properties){
        Session session = null;
        
        try {
            org.icatproject.icat.client.ICAT icatClient = new org.icatproject.icat.client.ICAT(properties.getICATUrl());
            session = icatClient.login(properties.getAuthenticator(), mapCredentials(properties.getReaderUserName(),properties.getReaderPassword()));
            LOG.info("Successfully creating a RESTFul client with the ICAT.");
        } catch (URISyntaxException | IcatException ex) {
            LOG.error("Issue creating the RestFul client ",ex);
        }
        return session;
    }
    
    /**
     * Maps the credentials for the ICAT RestFul login call.
     * @param userName name of the user account.
     * @param password of the user account.
     * @return a map containing the users name and password.
     */
    public static Map mapCredentials(String userName, String password){
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", userName);
        credentials.put("password", password);       
        return credentials;
        
    }    

    /**
    * Refreshes the ICAT session ID so the collector can keep connected to the ICAT
    * @throws IcatException_Exception Incase it can't connect to the ICAT
    */
    private void refreshSession(){
        try {
            LOG.info("Refresshing the Session ID");
            icat.refresh(sessionID);
            restSession.refresh();
        } catch (IcatException | IcatException_Exception ex) {
            LOG.error("Issue with refreshing the ICAT sessions ", ex);
        }
      
    }
    
    public String getAuthenticators(){
        return authenticators;
    }
    
    public String getSessionID(){
        return sessionID;
    }
    
    public Session getRestSession(){
        return restSession;
    }
    
    
    /**
     * Puts the username and password into a Credentials object.
     * @param userName for the reader account.
     * @param password for the reader account.
     * @return credentials object containing username and password.
     */
    public Login.Credentials getCredentials(String userName, String password){
        
        Login.Credentials credentials = new Login.Credentials(); 
        List<Login.Credentials.Entry> entries = credentials.getEntry(); 
        Login.Credentials.Entry e; 

        e = new Login.Credentials.Entry(); 
        e.setKey("username"); 
        e.setValue(userName); 
        entries.add(e); 
        e = new Login.Credentials.Entry(); 
        e.setKey("password"); 
        e.setValue(password); 
        entries.add(e);
        
        return credentials;
        
    }    
}
