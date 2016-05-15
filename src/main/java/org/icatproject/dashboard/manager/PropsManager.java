/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.manager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PropsManager will read the property file provided and parse the values inside it to 
 * the variables. Getters and setters are also made available.
 * 
 */
@Singleton
@Startup
public class PropsManager {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(PropsManager.class);
    private String ICATUrl;
    private String loginAuth;
    private String userName;
    private String password;
    private int collectionTime;
    
    private List<String> authorisedAccounts;
    
    private String topCatURL;    
    
    
    private String idsAddress;

   
    public PropsManager() {
        
    }
    
    @PostConstruct
    public void init(){
        collectProperties("dashboard.properties");
    }
    /**
     * Constructor that reads the property files and assigns the values to the variables.
     * @param propertyFile Name of the properties file
     */
    
    public void collectProperties(String propertyFile){   
        LOG.info("Reading properties.");
        File f = new File(propertyFile);
           Properties props = null;
           try {
               props = new Properties();
               props.load(new FileInputStream(f));
           } catch (Exception e) {
               String msg = "Unable to read property file " + f.getAbsolutePath() + "  "
                       + e.getMessage();
               LOG.error(msg);
               throw new IllegalStateException(msg);

           }
           
           ICATUrl = props.getProperty("icat.url").trim();
           loginAuth = props.getProperty("authenticator").trim();
           authorisedAccounts = Arrays.asList(props.getProperty("authorised_accounts").split("\\s+"));
           userName = props.getProperty("userName").trim();
           password = props.getProperty("password").trim();                      
           topCatURL = props.getProperty("topCatURL").trim(); 
           collectionTime = Integer.parseInt(props.getProperty("collectionTime").trim());
            
           
           LOG.info("Authorised accounts set as: "+authorisedAccounts);
           LOG.info("ICAT set as: "+ICATUrl);
           LOG.info("TopCat set as: "+topCatURL);
           LOG.info("Reader account set as "+userName);
           LOG.info("Collection time set as (24 Hour Clock) "+collectionTime);
           
           LOG.info("Finished collecting properties.");
          
           
    }
    
    //Getters for the methods.
    
    public int getCollectionTime(){
        return collectionTime;
    }
    
    public String getIdsAddress() {
        return idsAddress;
    }

    public String getTopCatURL() {
        return topCatURL;
    }

    public List<String> getAuthorisedAccounts() {
        return authorisedAccounts;
    }    
    
      
    public String getICATUrl(){
        return ICATUrl;
    }
    public String getAuthenticator(){
        return loginAuth;
    }
    public String getReaderUserName(){
        return userName;
    }
    public String getReaderPassword(){
        return password; 
    }
    
    
}
