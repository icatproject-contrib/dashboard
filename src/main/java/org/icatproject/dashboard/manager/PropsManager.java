/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.manager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
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
    
    private HashSet authorisedAccounts;
    private HashSet functionalAccounts;
    
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
           userName = props.getProperty("userName").trim();
           password = props.getProperty("password").trim();                      
           topCatURL = props.getProperty("topCatURL").trim(); 
           collectionTime = Integer.parseInt(props.getProperty("collectionTime").trim());          
           authorisedAccounts = getHashSetProperties("authorised_accounts",props);
           functionalAccounts = getHashSetProperties("functional_accounts",props);
                     
           
           LOG.info("Authorised accounts set as: "+authorisedAccounts);
           LOG.info("Functional accounts set as: "+functionalAccounts);
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

    public HashSet getAuthorisedAccounts() {
        return authorisedAccounts;
    }    

    public HashSet getFunctionalAccounts() {
        return functionalAccounts;
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
    
    /**
     * Gets the value from the properties. Converts to list then trims and pushes into 
     * a hashset.
     * @param propertyName Name of the property to extract.
     * @param props Properties object pointing to file dashboard.properties
     * @return A String HashSet containing the property values.
     */
    private HashSet getHashSetProperties(String propertyName, Properties props){
        
        List<String> list = Arrays.asList(props.getProperty(propertyName).split("\\s+"));
        
        HashSet convertedSet = new HashSet();
        
        for (String item : list) {
                String itemTrimmed = item.trim();
                convertedSet.add(itemTrimmed);
        }
        
        return convertedSet;
    }
    
    
}
