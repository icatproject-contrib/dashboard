/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.dashboard.core.entity.CollectionType;
import org.dashboard.core.manager.EntityBeanManager;
import org.icatproject.*;

/**
 * Will count the amount of entities found inside the ICAT.
 * 
 */
@Singleton
public class EntityCounter extends Collector {
    
    @EJB
    EntityBeanManager beanManager;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;
    
    private List<Object> instruments = new ArrayList();    
    private List<String> entities;
    
    
    
    private ICAT icat;
    private String sessionID;   
    
    DateTimeFormatter format;   
    
    
    public EntityCounter() {
    } 
    
    public void init(ICAT icat,String sessionID){
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.icat = icat;
        this.sessionID = sessionID;      
        
        collectInstruments();        
        
    }
    
    
    
    private void collectInstruments(){        
        try {
            //Add the standard Entities to be counted.
            entities = new ArrayList();
            entities.add("Investigation");
            entities.add("Dataset");
            entities.add("Datafile");
            instruments = icat.search(sessionID, "SELECT i.name FROM Instrument i");
            
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);            
        }        
    }    
    public void countEntities(LocalDate startDate, LocalDate endDate){
        for(Object inst :instruments){
            countDatafile(inst.toString(),startDate);
        }        
    }
    
   
    
    public int countDatafile(String instrument, LocalDate date){
        int count =0;            
                       
        try {
            
            String query="SELECT COUNT(d) FROM Datafile d WHERE d.createTime >{ts "+ date.format(format) +" 00:00:00 }";
            query += " AND d.createTime < {ts "+date.format(format)+" 23:59:59 }";
            List<Object> search = icat.search(sessionID,query);
            count = Integer.parseInt(search.get(0).toString());
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        return count;
    }
    
    @Override
    public void integerityUpdate(LocalDate date, boolean passed,CollectionType type){
        
    }
    
}
