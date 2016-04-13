/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.consumers;

import java.util.Date;
import java.util.List;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.icatproject.dashboard.collector.UserCollector;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.InternalException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
   
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),  
    @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "jms/ICAT/log"),
    @ActivationConfigProperty(propertyName= "destination", propertyValue="jms_ICAT_log"),
    @ActivationConfigProperty(propertyName="acknowledgeMode", propertyValue="Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability",propertyValue = "Durable"),   
    @ActivationConfigProperty(propertyName = "clientId",propertyValue = "icatDashboard2"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "dashboardSub"), 
    
    
  
    
})


public class ICATListener implements MessageListener {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ICATListener.class);
    
    @EJB
    private EntityBeanManager beanManager;
    
    @EJB
    private UserCollector userCollector;
    
    @EJB
    private PropsManager properties;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;   

    private ICATLog log;
    /**
     * Interface method that receives messages from the subscribed queue. Pulls of the information it
     * needs off the properties and creates the query object to be inserted into the dashboard.
     * @param message the JMS message
     */
    @Override
    public void onMessage(Message message) {
        
        TextMessage text = (TextMessage) message;       
      
        log = parseJMSMessage(text);
        
        updateUserInfo(log);
        
        addLocation(log);
        
       
        try {
            beanManager.create(log, manager);
        } catch (DashboardException ex) {
           logger.error("Issue inserting ICATLog into the dashboard ",ex);
        }
        
    }
    
    /**
     * Adds a geoLocation to the ICATLog if it isn't associated with a functional
     * account. Can have functional accounts doing millions of inserts so do not want
     * to call the API each time.
     * @param log to have it's location added.     
     */
    private void addLocation(ICATLog log){
        
        List<String> functionalAccounts = properties.getFunctionalAccounts();    

        Boolean addLocation = true;
        
        for(String account : functionalAccounts ){
            if(account.equals(log.getUser().getName())){
                addLocation = false;
                break;                
            }
        }       
       
        if(addLocation){
            log.setLocation(GeoTool.getGeoLocation(log.getIpAddress(), manager, beanManager));
        }
    }
    
    /**
     * Updates the current user status 
     * @param log the ICAT log.
     */
    private void updateUserInfo(ICATLog log){
        
        String operation = log.getOperation();
        ICATUser user = log.getUser();
        
        if("login".equals(operation)){
             user.setLogged(true);
        }
        else if("logout".equals(operation)){
            user.setLogged(false);
        }      
        
        beanManager.update(user, manager);
        
        
    }
    
    /**
     * Collects data from the ICAT JMS message and puts it into a ICATLog object.
     * @param message the message to have its data extracted.
     * @return a HashMap of data extracted from the message.
     */
    private ICATLog parseJMSMessage(TextMessage messageBody){
        ICATLog icatLog = new ICATLog();
        
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(messageBody.getText());
            JSONObject json = (JSONObject) obj;
            
            if(json.containsKey("userName")){
                icatLog.setUser(getUser((String)json.get("userName")));            
            }
            
            if(json.containsKey("entityName")){
                icatLog.setEntityType((String)json.get("entityName"));
            }
            
            if(json.containsKey("entityId")){
                icatLog.setEntityId((Long)json.get("entityId"));
            }
            
            if(json.containsKey("query")){
                icatLog.setQuery((String)json.get("query"));
            }                    
            
            
           icatLog.setIpAddress(messageBody.getStringProperty("ip"));
           
           icatLog.setDuration(messageBody.getLongProperty("millis"));
           
           icatLog.setOp(messageBody.getStringProperty("operation"));   
           
           icatLog.setLogTime(new Date(messageBody.getLongProperty("start")));
           
            
        } catch (ParseException | JMSException | InternalException ex) {
            logger.info("Issue with parsing the JMS message body: ",ex.getMessage());
        }        
        
        return icatLog;
        
    }
    
    
    
    /**
     * Retrieves the user from the dashboard. If the user is found in the dashboard then the user is inserted into the
     * dashboard before being returned.
     * @param name Unique name of the user in the ICAT
     * @return a dashboard User object.
     * @throws org.icatproject.dashboard.exceptions.InternalException
     */
    public ICATUser getUser(String name) throws InternalException{
        ICATUser dashBoardUser;
        
        List<Object> user = beanManager.search("SELECT u FROM ICATUser u WHERE u.name= '"+name+"'", manager);        
        
        if(user.get(0)==null){           
            dashBoardUser = userCollector.insertUser(name);           
        }
        else{           
            dashBoardUser = (ICATUser)user.get(0);
        }
        
        return dashBoardUser;        
    }
    
   
   
    
}