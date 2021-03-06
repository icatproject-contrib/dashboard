/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


@Comment("This is a item that has been downloaded via the IDS. It belongs to an Entity Collection")
@SuppressWarnings("serial")
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"TYPE","ICATID"})})
@Entity
@XmlRootElement
public class Entity_ extends EntityBaseBean implements Serializable{
    
    @Comment("The Download mapping.")    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    private List<DownloadEntity> downloadEntities;
    
    @Comment("ID of the entity in the ICAT")
    @Column( nullable = false)
    private Long icatId;
    
    @Comment("Name of the Entity.")
    @Column( nullable = false)
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

    public Long getIcatId() {
        return icatId;
    }
    
    public List<DownloadEntity> getDownloadEntities() {
        return downloadEntities;
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

    public void setIcatId(Long icatId) {
        this.icatId = icatId;
    }

    
    public void setDownloadEntities(List<DownloadEntity> downloadEntities) {
        this.downloadEntities = downloadEntities;
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
