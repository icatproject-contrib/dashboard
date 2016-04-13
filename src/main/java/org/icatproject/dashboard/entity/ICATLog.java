/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;


import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


@Comment("A Query")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
public class ICATLog extends EntityBaseBean implements Serializable {
    
    @Comment("The user which interacted with the ICAT.")
    @JoinColumn(name="USER_ID", nullable = false)
    @ManyToOne(fetch= FetchType.LAZY)
    private ICATUser user;   
    
    @Comment("Length of time the query took.")
    private Long duration;
    
    @Comment("Type of query that took place.")
    private String operation;
    
    @Comment("The query performed if there was one.")
    @Column(length = 4000)
    private String query;   
    
    @Comment("The id of the entity that modified/searched for.")
    private Long entityId;
    
    @Comment("The type of entity modified/searched for.")
    private String entityName;
    
    @Comment("The ip address of where the action took place.")
    private String ipAddress;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Time the activity took place.")
    private Date logTime;
    
    @Comment("A user can have a location")
    @JoinColumn(name="GEOLOCATION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private GeoLocation location;
    
    public ICATLog(){
        
    } 

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityName;
    }

    public void setEntityType(String entityType) {
        this.entityName = entityType;
    }
    
       
    
    public void setUser(ICATUser user) {
        this.user = user;
    }

    public void setDuration(Long Duration) {
        this.duration = Duration;
    }

    public void setOp(String operation) {
        this.operation = operation;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ICATUser getUser() {
        return user;
    }

    public Long getDuration() {
        return duration;
    }

    public String getOperation() {
        return operation;
    }

    public String getQuery() {
        return query;
    }
    
    
    
    
}
