/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.dashboard.core.entity.CollectionType;
import org.icatproject.ICAT;


public abstract class Collector {
    
   
    
    protected ICAT icat;
    protected String sessionID;   
    
    DateTimeFormatter format;
    
    public void init(ICAT icat, String sessionID){
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
    
    public abstract void integerityUpdate(LocalDate date, boolean passed,CollectionType type);
    
}
