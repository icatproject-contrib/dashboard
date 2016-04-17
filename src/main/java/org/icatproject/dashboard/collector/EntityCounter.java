/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.collector;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.ICATSessionManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.dashboard.entity.EntityCount;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.icat.client.Session;
import org.slf4j.LoggerFactory;

/**
 * Will count the number of entities found inside the ICAT.
 * 
 */
@Stateless
public class EntityCounter  {
    
    @EJB
    EntityBeanManager beanManager;
    
    @EJB
    private PropsManager properties;
    
    @EJB
    private ICATSessionManager sessionManager;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;    
     
    private final String[] ENTITIES={"Application", "DataCollection", "DataCollectionDatafile", "DataCollectionDataset", "DataCollectionParameter", "Datafile", "DatafileFormat", "DatafileParameter", "Dataset", "DatasetParameter", "DatasetType", "Facility", "FacilityCycle", "Grouping", "Instrument", "InstrumentScientist", "Investigation", "InvestigationGroup", "InvestigationInstrument", "InvestigationParameter", "InvestigationType", "InvestigationUser", "Job", "Keyword", "ParameterType", "PermissibleStringValue", "PublicStep", "Publication", "RelatedDatafile", "Rule", "Sample", "SampleParameter", "SampleType", "Shift", "Study", "StudyInvestigation", "User","UserGroup"};
    
    private final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
    
    private Session icatSession;
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EntityCounter.class);  
    
    
    
      
    @PostConstruct
    public void init(){
        icatSession = sessionManager.getRestSession();     
              
        
    }   
    
    
    /**
     * Starts the collection of entity information from the ICAT to the Dashboard.
     * @param startDate to work from.
     * @param endDate up to but no including this date.
     */
    public void performCollection(LocalDate startDate, LocalDate endDate){
       
        //Only want to go to the day before
        while(startDate.isBefore(endDate)){
            countEntities(startDate);
            
            
            
            startDate = startDate.plusDays(1);
        }
             
    }
    
    /***
     * Counts all of the ICAT entities created on a specific date.
     * @param countLocalDate the date to count entity creations.
     */
    public void countEntities(LocalDate countLocalDate){ 
        
        LOG.info("Starting entity count for date ", countLocalDate.toString());
        
        Date countDate = convertToDate(countLocalDate);
        
        for(String entity : ENTITIES){
            String countQuery = "SELECT COUNT(entity.id) FROM"+entity+" as entity WHERE entity.createTime >= {ts "+countLocalDate.toString()+" 00:00:00} AND entity.createTime <= {ts "+countLocalDate.toString()+" 23:59:59}";
            
            try {
                String result = icatSession.search(countQuery);
                
                if(!result.equals("[]")){
                    EntityCount ec = new EntityCount(countDate,entity,Long.parseLong(result));
                    beanManager.create(ec, manager);
                }
                
            } catch (org.icatproject.icat.client.IcatException | DashboardException ex) {
                LOG.error("A error has occured counting entities ",ex);
            }
        }
        
        
        LOG.info("Completed entity count for date ", countLocalDate.toString());
        
    }
    
    private void collectInstrumentMeta(LocalDate collectionLocalDate){
         LOG.info("Starting Instrument meta data collection for ", collectionLocalDate.toString());
         
         String instrumentQuery = "SELECT instrument.id, COUNT(datafile.id), SUM(datafile.fileSize) FROM Datafile as datafile "
                 + "JOIN datafile.dataset dataset JOIN dataset.investigation investigation JOIN investigation.investigationInstrument investigationInstrument"
                 + "WHERE datafile.createTime >= {ts "+collectionLocalDate.toString()+" 00:00:00} AND datafile.createTime <= {ts "+collectionLocalDate.toString()+" 23:59:59}"
                 + "GROUP BY instrument.id ";
         
        try {
            String result = icatSession.search(instrumentQuery);
            
        } catch (org.icatproject.icat.client.IcatException ex) {
            LOG.error("Issue with instrument meta collection ", ex);
        }
         
         
         LOG.info("Completed Instrument meta data collection for ", collectionLocalDate.toString());
    }
    
    
    
    /***
    * Converts a LocalDate object to a Java.util.date object.
    * @param localDate to convert.
    * @return a Date object.
    */
    private Date convertToDate(LocalDate localDate){
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
        
    }
    
 
   
    
    
   
    
    
    
}
