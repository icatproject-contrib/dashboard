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
    
    
    private static final Logger log = LoggerFactory.getLogger(PropsManager.class);
    private List<String> functionalAccounts;
    private String ICATUrl;
    private String loginAuth;
    private String userName;
    private String password;
    private int collectTime;
    
    private String topCatURL;    
    private String topCatUser;
    private String topCatPass;
    
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
        log.info("Reading properties.");
        File f = new File(propertyFile);
           Properties props = null;
           try {
               props = new Properties();
               props.load(new FileInputStream(f));
           } catch (Exception e) {
               String msg = "Unable to read property file " + f.getAbsolutePath() + "  "
                       + e.getMessage();
               log.error(msg);
               throw new IllegalStateException(msg);

           }
           functionalAccounts = Arrays.asList(props.getProperty("functional_accounts").split("\\s+"));
           ICATUrl = props.getProperty("ICAT");
           loginAuth = props.getProperty("authenticator");
           
           userName = props.getProperty("userName");
           password = props.getProperty("password");
           collectTime = Integer.parseInt(props.getProperty("collect_Time"));
           
           topCatURL = props.getProperty("topCatURL");
           //Requires Trim incase of escaping white characters.
           topCatUser = (props.getProperty("topCatUser")).trim();
           topCatPass = (props.getProperty("topCatPass")).trim();
           
           idsAddress = props.getProperty("idsAddress");
           
           
    }
    
    //Getters for the methods.
    
    public String getIdsAddress() {
        return idsAddress;
    }

    public String getTopCatURL() {
        return topCatURL;
    }

    public String getTopCatUser() {
        return topCatUser;
    }

    public String getTopCatPass() {
        return topCatPass;
    }
    
    public List<String> getAccounts(){
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
    public int getCollectTime(){
        return collectTime;
    }

    
}
