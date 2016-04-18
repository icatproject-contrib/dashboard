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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.IcatDataManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.dashboard.entity.EntityCount;
import org.icatproject.dashboard.entity.ImportCheck;
import org.icatproject.dashboard.entity.InstrumentMetaData;
import static org.icatproject.dashboard.entity.InstrumentMetaData_.instrumentId;
import org.icatproject.dashboard.entity.InvestigationMetaData;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
    private IcatDataManager sessionManager;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;    
     
    private final String[] ENTITIES={"Application", "DataCollection", "DataCollectionDatafile", "DataCollectionDataset", "DataCollectionParameter", "Datafile", "DatafileFormat", "DatafileParameter", "Dataset", "DatasetParameter", "DatasetType", "Facility", "FacilityCycle", "Grouping", "Instrument", "InstrumentScientist", "Investigation", "InvestigationGroup", "InvestigationInstrument", "InvestigationParameter", "InvestigationType", "InvestigationUser", "Job", "Keyword", "ParameterType", "PermissibleStringValue", "PublicStep", "Publication", "RelatedDatafile", "Rule", "Sample", "SampleParameter", "SampleType", "Shift", "Study", "StudyInvestigation", "User","UserGroup"};
       
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
            
            Date currentDate = convertToDate(startDate);
            
            boolean countPassed = countEntities(startDate);
            boolean instrumentPassed = collectInstrumentMeta(startDate);
            boolean investigationPassed = collectInvestigationMeta(startDate); 
            
            insertImportCheck(currentDate, countPassed, "entityCount");
            insertImportCheck(currentDate,instrumentPassed,"instrumentMeta");
            insertImportCheck(currentDate,investigationPassed,"investigationMeta");            
            
            startDate = startDate.plusDays(1);
        }
             
    }
    
    /***
     * Counts all of the ICAT entities created on a specific date.
     * @param countLocalDate the date to count entity creations.
     * @return if it has passed without issues
     */
    public boolean countEntities(LocalDate countLocalDate){ 
        boolean passed = true;
        
        LOG.info("Starting entity count for date ", countLocalDate.toString());
        
        Date countDate = convertToDate(countLocalDate);
        
        JSONParser parser = new JSONParser();
        
        
        for(String entity : ENTITIES){
            String countQuery = "SELECT COUNT(entity.id) FROM "+entity+" as entity WHERE entity.createTime >= {ts "+countLocalDate.toString()+" 00:00:00} AND entity.createTime <= {ts "+countLocalDate.toString()+" 23:59:59}";
            JSONArray resultArray;
            try {
                String icatResult = icatSession.search(countQuery);
                resultArray = (JSONArray) parser.parse(icatResult);
                
                Long countValue = (Long) resultArray.get(0);
                
                if(countValue!=0){
                    EntityCount ec = new EntityCount(countDate,entity,countValue);
                    beanManager.create(ec, manager);
                }
                
            } catch (org.icatproject.icat.client.IcatException | DashboardException | ParseException ex) {
                LOG.error("A error has occured counting entities ",ex);
                passed = false;
            }
        }        
        
        LOG.info("Completed entity count for date ", countLocalDate.toString());
        
        return passed;
    }
    
    private boolean collectInstrumentMeta(LocalDate collectionLocalDate){
         LOG.info("Starting Instrument meta data collection for ", collectionLocalDate.toString());
         
         boolean passed = true;
         
         Date collectionDate = convertToDate(collectionLocalDate);
         
         String instrumentQuery = "SELECT instrument.id, COUNT(datafile.id), SUM(datafile.fileSize) FROM Datafile as datafile "
                 + "JOIN datafile.dataset dataset JOIN dataset.investigation investigation JOIN investigation.investigationInstruments investigationInstrument JOIN investigationInstrument.instrument instrument "
                 + "WHERE datafile.createTime>= {ts "+collectionLocalDate.toString()+" 00:00:00} AND datafile.createTime<= {ts "+collectionLocalDate.toString()+" 23:59:59} "
                 + "GROUP BY instrument.id ";
         
        try {
            String result = icatSession.search(instrumentQuery);
            JSONParser parser = new JSONParser();
            JSONArray resultArray;
            
            resultArray = (JSONArray) parser.parse(result);
            
            if(resultArray.size()>0){
                
                for (Iterator it = resultArray.iterator(); it.hasNext();) {
                 JSONArray sub = (JSONArray) it.next();
                 long instrumentId = (long) sub.get(0);
                 long dataFileCount = (long) sub.get(1);
                 long dataFileVolume = (long) sub.get(2);
                 
                 InstrumentMetaData insData = new InstrumentMetaData(dataFileCount, dataFileVolume, collectionDate, instrumentId);
                 beanManager.create(insData, manager);
                 
                }
                
            } 
            
             
            
        } catch (org.icatproject.icat.client.IcatException | DashboardException | ParseException ex) {
            LOG.error("Issue with instrument meta collection ", ex);
            passed = false;
        } 
         
         
         LOG.info("Completed Instrument meta data collection for ", collectionLocalDate.toString());
         
         return passed;
    }
    
    
    private boolean collectInvestigationMeta(LocalDate collectionLocalDate){
        LOG.info("Starting Instrument meta data collection for ", collectionLocalDate.toString());
        
        boolean passed = true;
        
        Date collectionDate = convertToDate(collectionLocalDate);
         
         String investigationQuery = "SELECT investigation.id, COUNT(datafile.id), SUM(datafile.fileSize) FROM Datafile as datafile "
                 + "JOIN datafile.dataset dataset JOIN dataset.investigation investigation "
                 + "WHERE datafile.createTime >= {ts "+collectionLocalDate.toString()+" 00:00:00} AND datafile.createTime <= {ts "+collectionLocalDate.toString()+" 23:59:59} "
                 + "GROUP BY investigation.id ";
         
        try {
            String result = icatSession.search(investigationQuery);
            JSONParser parser = new JSONParser();
            JSONArray resultArray;
            
            resultArray = (JSONArray) parser.parse(result);
            
            if(resultArray.size()>0){
                
                for (Iterator it = resultArray.iterator(); it.hasNext();) {
                 JSONArray sub = (JSONArray) it.next();
                 long investigationId = (long) sub.get(0);
                 long dataFileCount = (long) sub.get(1);
                 long dataFileVolume = (long) sub.get(2);
                 
                 InvestigationMetaData invData = new InvestigationMetaData(dataFileCount, dataFileVolume, collectionDate, investigationId);
                 
                 beanManager.create(invData, manager);
                 
                }
                
            } 
        } catch (org.icatproject.icat.client.IcatException  | DashboardException | ParseException ex) {
            LOG.error("Issue with instrument meta collection ", ex);
            passed = false;
        }
         
         LOG.info("Completed Instrument meta data collection for ", collectionLocalDate.toString());
         
         return passed;
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
    
    
    private void insertImportCheck(Date date,boolean passed,String importType){
        
        ImportCheck check = new ImportCheck(date,passed,importType);
        
        try {
            beanManager.create(check, manager);
        } catch (DashboardException ex) {
            LOG.error("Issue inserting import check into dashboard ", ex);
        }
        
    }
 
   
    
    
   
    
    
    
}
