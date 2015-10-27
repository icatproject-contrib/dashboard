/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



@Comment("Counts the amount of entites based on their association with an instrument.")
@Entity
public class EntityCount extends EntityBaseBean implements Serializable {
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Date of the Entity Value.")
    private Date countDate;
    
    @Comment("Entity type of the Count e.g. Investigation, Datafile etc...")
    private String entity;
    
    @Comment("Instrument that the Entity count is associated with.")
    private String instrument;
    
    public EntityCount(){
        
    }

    public Date getDate() {
        return countDate;
    }

    public String getEntity() {
        return entity;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setDate(Date date) {
        this.countDate = date;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
    
    
}
