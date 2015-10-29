/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.dashboard.core.entity.CollectionType;
import org.dashboard.core.manager.EntityBeanManager;
import org.icatproject.ICAT;


public abstract class Collector {
    
   
    
    protected ICAT icat;
    protected String sessionID;   
    
    DateTimeFormatter format;
    
    public void init(){
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.icat = icat;
        this.sessionID = sessionID;
    }
    
    /**
     * Updates the sessionID once a refresh from the DataCollector has taken place.
     * @param sessionID 
     */
    public void refresh(String sessionID){
        this.sessionID = sessionID;
    }
    
    public abstract void integerityUpdate(Date date, boolean passed,CollectionType type);
    
}
