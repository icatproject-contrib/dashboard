/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
@Table(uniqueConstraints= {@UniqueConstraint(columnNames = {"USER_ID,ENTITYCOLLECTION_ID"})})
public class Download extends EntityBaseBean implements Serializable{
    
    @Comment("A download is associated with a user.")
    @JoinColumn(name = "USER_ICAT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;    
    
    @Comment("A download has a Entity collection")
    @OneToOne(cascade = CascadeType.ALL, mappedBy="download")
    private EntityCollection entityCollection;
    
    @Comment("The preparedID from TopCAT")
    private UUID preparedID;
    
    @Comment("The time the download took place.")
    private Date downloadTime;
    
    @Comment("Ip address of where the download took place.")
    private InetAddress ipAddress;
    
    @Comment("The method of download.")
    private String method;
    
    public Download(){
        
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEntityCollection(EntityCollection entityCollection) {
        this.entityCollection = entityCollection;
    }

    public void setPreparedID(UUID preparedID) {
        this.preparedID = preparedID;
    }

    public void setDownloadTime(Date downloadTime) {
        this.downloadTime = downloadTime;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public User getUser() {
        return user;
    }

    public EntityCollection getEntityCollection() {
        return entityCollection;
    }

    public UUID getPreparedID() {
        return preparedID;
    }

    public Date getDownloadTime() {
        return downloadTime;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getMethod() {
        return method;
    }
    
}
