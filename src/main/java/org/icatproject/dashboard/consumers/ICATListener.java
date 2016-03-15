/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.consumers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.icatproject.dashboard.collector.UserCollector;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.entity.Query;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.InternalException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.slf4j.LoggerFactory;
   
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName="maxSessions",propertyValue="1"),
    @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "jms/IDS/log"),
    @ActivationConfigProperty(propertyName= "destination", propertyValue="jms_IDS_log"),
    @ActivationConfigProperty(propertyName="acknowledgeMode", propertyValue="Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability",propertyValue = "Durable"),   
    @ActivationConfigProperty(propertyName = "clientId",propertyValue = "dashboardID4"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "dashboardSub") 
    
})

public class ICATListener implements MessageListener {
    
    @EJB
    private EntityBeanManager beanManager;
    
    @EJB
    private UserCollector userCollector;
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ICATListener.class);
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;   

    
    /**
     * Interface method that receives messages from the subscribed queue. Pulls of the information it
     * needs off the properties and creates the query object to be inserted into the dashboard.
     * @param message the JMS message
     */
    @Override
    public void onMessage(Message message) {
        try {
            Query icatQuery = new Query();
             
            ObjectMessage om = (ObjectMessage) message;      
            
            icatQuery.setUser(getUser(om.getStringProperty("user")));
            icatQuery.setDuration(om.getLongProperty("duration"));
            icatQuery.setQueryID(om.getLongProperty("id"));
            icatQuery.setQuery(om.getStringProperty("query"));
            
            createQuery(icatQuery);          
            
            
        } catch (JMSException | InternalException ex) {
            log.error("Issue with processing the ICAT message");
        }
    }
    
    /**
     * Inserts the query into the dashboard database.
     * @param q The query object to be inserted
     */
    public void createQuery(Query q){
        try {
            beanManager.create(q, manager);
        } catch (DashboardException ex) {
            Logger.getLogger(ICATListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Retrieves the user from the dashboard. If the user is found in the dashboard then the user is inserted into the
     * dashboard before being returned.
     * @param name Unique name of the user in the ICAT
     * @return a dashboard User object.
     */
    public ICATUser getUser(String name) throws InternalException{
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
    
   
   
    
}
