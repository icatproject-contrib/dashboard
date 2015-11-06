/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.namespace.QName;
import org.dashboard.core.entity.CollectionType;
import org.dashboard.core.entity.Entity_;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.ICATSessionManager;
import org.dashboard.core.manager.PropsManager;
import org.icatproject.*;

/**
 * Will count the amount of entities found inside the ICAT.
 * 
 */
@Stateless
public class EntityCounter  {
    
    @EJB
    EntityBeanManager beanManager;
    
    @EJB
    private PropsManager prop;
    
    @EJB
    private ICATSessionManager session;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;
    
    private List<Object> instruments = new ArrayList();    
    private List<String> entities;
    
    
    
    private ICAT icat;
    private String sessionID;   
    
    DateTimeFormatter format;   
    
    
    public EntityCounter() {
    } 
    
    @PostConstruct
    public void init(){
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        icat = createICATLink();
        sessionID = session.getSessionID();         
        collectInstruments();        
        
    }
    
    public ICAT createICATLink(){
        ICAT icat = null;
         try {
            URL hostUrl;
            
            hostUrl = new URL("https://"+prop.getICATUrl());
            URL icatUrl = new URL(hostUrl, "ICATService/ICAT?wsdl");
            QName qName = new QName("http://icatproject.org", "ICATService");
            ICATService service = new ICATService(icatUrl, qName);
            icat = service.getICATPort();            
                                        
                    
        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        return icat;
        
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
        while(startDate.isBefore(endDate)){
            for(Object inst :instruments){
                countDatafile(inst.toString(),startDate);
            }        
        }
    }
    
   /*
    Query Graveyard:
    JOIN d.dataset ds JOIN ds.investigation dsi JOIN dsi.investigationInstruments ii JOIN ii.instrument i ";
            query+= "WHERE i.name= '"+instrument+"'";
            query+= " AND d.createTime >{ts 2015-10-14 00:00:00 }";
    
    */
    
    public int countDatafile(String instrument, LocalDate date){
        int count =0;            
                       
        try {            
            String query="SELECT COUNT(d) FROM Datafile d WHERE d.createTime > {ts "+ date.format(format) +" 00:00:00 }";
            query += " AND d.createTime < {ts "+date.format(format)+" 00:00:00";
            List<Object> search = icat.search(sessionID,query);
            count = Integer.parseInt(search.get(0).toString());
        } catch (IcatException_Exception ex) {
            Logger.getLogger(EntityCounter.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        return count;
    }
    
    
    public void integerityUpdate(LocalDate date, boolean passed,CollectionType type){
        
    }
    
   
    
    
    
}
