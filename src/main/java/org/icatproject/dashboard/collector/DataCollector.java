/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.collector;

import org.icatproject.dashboard.manager.PropsManager;
import java.time.LocalDate;
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
import org.icatproject.dashboard.entity.ImportCheck;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@DependsOn("IcatDataManager")
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

    private static final Logger LOG = LoggerFactory.getLogger(DataCollector.class);

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
    protected void createTimer(int collectionTime) {

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
        checkForFailedImports();
        
    } 
    
  

    /***
     * Initialises the collection of entity information from the ICAT into the Dashboard.
     */
    private void initialiseEntityCollection() {  
        
        LocalDate today = LocalDate.now();
      
        LOG.info("Data Collection initiated for "+today.toString());
        
        LocalDate earliestEntityImport = getNextImportDate("entity");   
        LocalDate earliestInstrumentImport = getNextImportDate("instrument"); 
        LocalDate earliestInvestigationImport = getNextImportDate("investigation"); 
        
        
        //An actual import has happened. 
        if(earliestEntityImport!=null){
            counter.performEntityCountCollection(earliestEntityImport, today);
        } 
        if(earliestInstrumentImport!=null){
            counter.performInstrumentMetaCollection(earliestInstrumentImport, today);
        }
        if(earliestInvestigationImport!=null){
            counter.performInvestigationMetaCollection(earliestInvestigationImport, today);
        }
               
        LOG.info("Data collection completed for "+today.toString());
        
       
        
      
        
        
    } 
    
    /**
     * Checks to see if there have been any failed imports. If any are found then they are sent to be re-done.
     */
    public void checkForFailedImports(){
         LOG.info("Check for missed data collections.");
        Query failedImprotQuery = manager.createQuery("SELECT ic FROM ImportCheck ic WHERE ic.passed=0");
        
        List<Object> failedImports = failedImprotQuery.getResultList();
        
        if(!failedImports.isEmpty()){
            
            for(Object result : failedImports){
                
                ImportCheck ic = (ImportCheck) result;
                String type = ic.getType();
                LocalDate failedDate = convertToLocalDate(ic.getCheckDate());
                
                LOG.info("Found a failed import for "+type+" on the "+failedDate.toString());
                
                if("instrument".equals(type)){
                    counter.performInstrumentMetaCollection(failedDate, failedDate.plusDays(1));
                }else if("investigation".equals(type)){
                     counter.performInvestigationMetaCollection(failedDate, failedDate.plusDays(1));
                    
                }else if("entity".equals(type)){
                    counter.performEntityCountCollection(failedDate, failedDate.plusDays(1));
                    
                }
                
                
                
            }
            
            
        }
        
        LOG.info("Finished checking for missed data collections.");
        
    }
    
    
    
    
    /**
     * Finds out the earliest LocalDate that an import check has been inserted and then
     * returns the next day
     * @return the date of when to perform the next import. If no check was found then 
     * null is returned.
     */
    public LocalDate getNextImportDate(String type){
        
        Query importCheckQuery = manager.createQuery("SELECT ic.checkDate FROM ImportCheck ic WHERE ic.passed=1 AND ic.type='"+type+"' ORDER BY ic.checkDate desc");
                    
        importCheckQuery.setMaxResults(1);
        //Have to use getResultList as getSingleResult will fail if no collection has occured.        
        List<Object> dates = importCheckQuery.getResultList();
        
        LocalDate earliestImport = null;
        
        if(!dates.isEmpty()){
            earliestImport =  convertToLocalDate((Date)dates.get(0));
            earliestImport = earliestImport.plusDays(1);
        }                
                
        return earliestImport;
    
    }
    
      
    
    
    
}
