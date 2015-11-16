/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


@Comment("This is a item that has been downloaded via the IDS. It belongs to an Entity Collection")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints ={ @UniqueConstraint(columnNames = {"ENTITYCOLLECTION_ID"})})
@XmlRootElement
public class Entity_ extends EntityBaseBean implements Serializable{
    
    @Comment("The Collection the Item belongs to.")
    @JoinColumn(name="ENTITYCOLLECTION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityCollection entityCollection;
    
    
    @Comment("Name of the Entity.")
    private String entityName;
    
    @Comment("Size of the Entity in bytes.")
    private Long entitySize;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Time of creation of the Item in the ICAT.")
    private Date ICATcreationTime;
    
    @Comment("Type of Entity it is e.g. Investigatio, Datafile, Dataset... etc")
    private String type;
    
    public Entity_(){
        
    }

    public EntityCollection getEntityCollection() {
        return entityCollection;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntitySize() {
        return entitySize;
    }

    public Date getICATcreationTime() {
        return ICATcreationTime;
    }

    public String getType() {
        return type;
    }

    public void setEntityCollection(EntityCollection entityCollection) {
        this.entityCollection = entityCollection;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setEntitySize(Long entitySize) {
        this.entitySize = entitySize;
    }

    public void setICATcreationTime(Date ICATcreationTime) {
        this.ICATcreationTime = ICATcreationTime;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
}