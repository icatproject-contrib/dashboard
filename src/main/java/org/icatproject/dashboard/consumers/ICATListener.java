/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.consumers;

import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.icatproject.IcatException_Exception;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.manager.UserManager;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.InternalException;
import org.icatproject.dashboard.exceptions.GetLocationException;

import org.icatproject.dashboard.manager.EntityBeanManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;


@MessageDriven
public class ICATListener implements MessageListener {
    
    private final GeoLocation dummyLocation = new GeoLocation(54.3739, 2.9376, "GB", "Windermere", "Dummy ISP");
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ICATListener.class);
    
    @EJB
    private EntityBeanManager beanManager;
    
    @EJB
    private UserManager userCollector;     
    
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
        try {
            LOG.debug("Recieved a JMS message from the ICAT");
            
            TextMessage text = (TextMessage) message;
            
            LOG.info("JMS Message Recieved: \n" + text.toString());

            log = parseJMSMessage(text);

            updateUserInfo(log);

            addLocation(log);
       
            beanManager.create(log, manager);
        } catch (DashboardException | ParseException | JMSException | IcatException_Exception ex) {
           LOG.error("Issue inserting ICATLog into the dashboard ", ex);
        }
    }
    
    
    
    /**
     * Adds a geoLocation to the ICATLog.
     * @param log to have it's location added.     
     */
    private void addLocation(ICATLog log){
        try {
            log.setLocation(GeoTool.getGeoLocation(log.getIpAddress(), manager, beanManager));
        }
        catch (GetLocationException ex) {
            /* Finding the location has failed. Must set to the dummy location to make sure the download is still added to Dashboard.
             * Don't need to create a bean manager for this as it's only a dummy value anyway.
             */
            LOG.error(ex.getMessage() + " ipAddress: " + ex.getIpAddress());
            log.setLocation(dummyLocation);
        }
        
    }
    
    /**
     * Updates the current user status 
     * @param log the ICAT log.
     */
    private void updateUserInfo(ICATLog log){
        
        String operation = log.getOperation();
        ICATUser user = log.getUser();
        
        if("login".equals(operation)||"refresh".equals(operation)){
             user.setLogged(true);
             beanManager.update(user, manager);
        }
        else if("logout".equals(operation)){
            user.setLogged(false);
            beanManager.update(user, manager);
        }      
    }
    
    /**
     * Collects data from the ICAT JMS message and puts it into a ICATLog object.
     * @param message the message to have its data extracted.
     * @return a HashMap of data extracted from the message.
     */
    private ICATLog parseJMSMessage(TextMessage messageBody) throws ParseException, JMSException, IcatException_Exception, InternalException, DashboardException {
        ICATLog icatLog = new ICATLog();
        
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
            String query = (String)json.get("query");
            if(query.length() > 4000){
                query = query.substring(0, 3990) + "...";
            }
            icatLog.setQuery(query);
        }                    


       icatLog.setIpAddress(messageBody.getStringProperty("ip"));

       icatLog.setDuration(messageBody.getLongProperty("millis"));

       icatLog.setOp(messageBody.getStringProperty("operation"));   

       icatLog.setLogTime(new Date(messageBody.getLongProperty("start")));       
        
       return icatLog;
        
    }
    
    
    
    /**
     * Retrieves the user from the dashboard. If the user is found in the dashboard then the user is inserted into the
     * dashboard before being returned.
     * @param name Unique name of the user in the ICAT
     * @return a dashboard User object.
     * @throws org.icatproject.dashboard.exceptions.InternalException
     */
    public ICATUser getUser(String name) throws InternalException, IcatException_Exception, DashboardException {
        ICATUser dashBoardUser;
        
        List<Object> user = beanManager.search("SELECT u FROM ICATUser u WHERE u.name= '"+name+"'", manager);        
        
        if(user.isEmpty()){           
            dashBoardUser = userCollector.insertUser(name);           
        }
        else{           
            dashBoardUser = (ICATUser)user.get(0);
        }
        
        return dashBoardUser;        
    }
}