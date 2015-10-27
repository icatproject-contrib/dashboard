/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * PropsManager will read the property file provided and parse the values inside it to 
 * the variables. Getters and setters are also made avaiable.
 * @author yqa41233
 */
public class PropsManager {
    
    
    private static final Logger log = Logger.getLogger(PropsManager.class);
    private final List<String> functionalAccounts;
    private final String ICATUrl;
    private final String loginAuth;
    private final String userName;
    private final String password;
    private final int collectTime;
    
    /**
     * Constructor that reads the property files and assigns the values to the variables.
     * @param propertyFile Name of the properties file
     */
    public PropsManager(String propertyFile){    
        File f = new File("dashboard.properties");
           Properties props = null;
           try {
               props = new Properties();
               props.load(new FileInputStream(f));
           } catch (Exception e) {
               String msg = "Unable to read property file " + f.getAbsolutePath() + "  "
                       + e.getMessage();
               log.fatal(msg);
               throw new IllegalStateException(msg);

           }
           functionalAccounts = Arrays.asList(props.getProperty("functional_accounts").split("\\s+"));
           ICATUrl = props.getProperty("ICAT");
           loginAuth = props.getProperty("authenticator");
           userName = props.getProperty("userName");
           password = props.getProperty("password");
           collectTime = Integer.parseInt(props.getProperty("collect_Time"));
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
