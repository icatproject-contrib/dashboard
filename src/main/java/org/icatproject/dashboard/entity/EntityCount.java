package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;



@Comment("Counts the number of entites based on their association with an instrument.")
@Entity
@XmlRootElement
public class EntityCount extends EntityBaseBean implements Serializable {
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Date of the Entity Value.")
    private Date countDate;
    
    @Comment("Entity type of the Count e.g. Investigation, Datafile etc...")
    private String entityType;
    
    @Comment("The number of entities for the day")
    private Long entityCount;
    
    public EntityCount(){
        
    }
    
    public EntityCount(Date countDate, String entityType, Long entityCount) {
        this.countDate = countDate;
        this.entityType = entityType;
        this.entityCount = entityCount;
    }

    public Date getCountDate() {
        return countDate;
    }

    public void setCountDate(Date countDate) {
        this.countDate = countDate;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(Long entityCount) {
        this.entityCount = entityCount;
    }    
    
}
