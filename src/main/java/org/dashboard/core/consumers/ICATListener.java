/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.consumers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.dashboard.core.collector.UserCollector;
import org.dashboard.core.entity.ICATUser;
import org.dashboard.core.entity.Query;
import org.dashboard.core.manager.DashboardException;
import org.dashboard.core.manager.EntityBeanManager;
   
@TransactionManagement(TransactionManagementType.BEAN)
public class ICATListener implements MessageListener {
    
    @EJB
    private EntityBeanManager beanManager;
    
    @EJB
    private UserCollector userCollector;
    
    @Resource
    private UserTransaction userTransaction;

    
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ICATListener.class);
    
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
            
            
        } catch (JMSException ex) {
            Logger.getLogger(ICATListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Inserts the query into the dashboard database.
     * @param q The query object to be inserted
     */
    public void createQuery(Query q){
        try {
            beanManager.create(q, manager,userTransaction);
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
    
   
   
    
}
