/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entityManager.core.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;



@Comment("Counts the amount of entites based on their association with an instrument.")
@Entity
public class EntityCount extends EntityBaseBean implements Serializable {
    
    @Comment("Date of the Entity Value.")
    private Date date;
    
    @Comment("Entity type of the Count e.g. Investigation, Datafile etc...")
    private String entity;
    
    @Comment("Instrument that the Entity count is associated with.")
    private String instrument;
    
    public EntityCount(){
        
    }

    public Date getDate() {
        return date;
    }

    public String getEntity() {
        return entity;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
    
    
}
