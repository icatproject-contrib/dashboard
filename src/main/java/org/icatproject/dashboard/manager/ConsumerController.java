/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.manager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.icatproject.dashboard.collector.DataCollector;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
/**
 * Singleton to control when the JMS consumers should insert data into the Dashboard.
 * 
 */
public class ConsumerController {
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;    
    
    //Flag to stop the consumers from ingesting data till the import is complete.
    private boolean dataImported;
    
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DataCollector.class);
    
    @PostConstruct
    private void init() {
        dataImported = false;
        checkImport();
                
    }
    
   
    /**
     * Looks to see if the import flags have been inserted. If not sleeps for 10
     */
    private void checkImport(){
        Query importCheckQuery = manager.createQuery("SELECT ic FROM ImportCheck ic");
        
        while(!dataImported){
            List<Object> importResult = importCheckQuery.getResultList();
            
            if(importResult.isEmpty()){
                try {
                    Thread.sleep(100000);
                } catch (InterruptedException ex) {
                    LOG.error("Issue controller the Consumer Controller ",ex);
                }
                
            }else{
                dataImported = true;
            }
        }
    }
    
    public boolean getImportFlag(){
        return dataImported;
    }
    
    
}
