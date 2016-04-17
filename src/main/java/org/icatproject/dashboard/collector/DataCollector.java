/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.collector;

import org.icatproject.dashboard.manager.PropsManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.icatproject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@DependsOn("ICATSessionManager")
@Singleton
@Startup
public class DataCollector {

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    @EJB
    private PropsManager prop;
    
    @EJB
    private EntityCounter counter;

    protected ICAT icat;
    protected String sessionID;

    DateTimeFormatter format;

    private static final Logger log = LoggerFactory.getLogger(DataCollector.class);

    @Resource
    private TimerService timerService;
        
    /**
     * Init method is called once the EJB has been loaded. Does the initial
     * property collections and login into ICAT. Also initiates initial data
     * collection.
     */
    @PostConstruct
    private void init() {
        createTimer(prop.getCollectionTime());
                
        initialiseEntityCollection();

    }
    
    /**
     * Creates the timers for the daily data collection.
     * @param collectionTime the hour on which the collection will commence.
     */
    private void createTimer(int collectionTime) {

        TimerConfig dataCollect = new TimerConfig("dataCollect", false);
        timerService.createCalendarTimer(new ScheduleExpression().hour(collectionTime), dataCollect);

    } 
   

    /**
     * Handles the timers. If statement inside decides what timer was called and
     * what method should be invoked to deal with that timer. Currently only
     * refresh session and collection of data.
     *
     * @param timer is the object that is invoked when the timer service is
     * invoked.
     */
    @Timeout
    public void timeout(Timer timer) {
       
        initialiseEntityCollection();
        
    } 
    
   
    private LocalDate toLocalDate(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate(); 
        
        return localDate;
    }

    /***
     * Initialises the collection of entity information from the ICAT into the Dashboard.
     */
    private void initialiseEntityCollection() {  
        
        LocalDate today = LocalDate.now();
      
        log.info("Entity Collection initiated for ",today.toString());
        
        LocalDate earliestImport = earliestImportPass();
        
        //An actual import has happened
        if(earliestImport!=null){
            counter.performCollection(earliestImport, today);
        }               
               
        log.info("Entity collection completed for ",today.toString());
    }  
    
    
    /**
     * Finds out the earliest LocalDate that an import check has been inserted
     * @return the date of the earliest import check. If no check was found then 
     * null is returned.
     */
    private LocalDate earliestImportPass(){
        
        Query importCheckQuery = manager.createQuery("SELECT ic.checkDate FROM ImportCheck ic WHERE ic.passed=1 ORDER BY ic.checkDate desc");
                    
        importCheckQuery.setMaxResults(1);
        //Have to use getResultList as getSingleResult will fail if no collection has occured.        
        List<Object> dates = importCheckQuery.getResultList();
        
        LocalDate earliestImport = null;
        
        if(!dates.isEmpty()){
            earliestImport =  toLocalDate((Date)dates.get(0));
        }                
                
        return earliestImport;
    
    }
    
      
    
    
    
}
