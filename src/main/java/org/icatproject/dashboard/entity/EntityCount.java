package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;



@Comment("Counts the number of entites based on their association with an instrument.")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "COUNTDATE", "ENTITYTYPE"}) })
@Entity
@XmlRootElement
public class EntityCount extends EntityBaseBean implements Serializable {
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Date of the Entity Value.")
    @Column( nullable = false)
    private Date countDate;
    
    @Comment("Entity type of the Count e.g. Investigation, Datafile etc...")    
    @Column( nullable = false)
    private String entityType;
    
    @Comment("The number of entities for the day")
    @Column( nullable = false)
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
