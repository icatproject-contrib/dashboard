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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

@Comment("A download is the process of saving entities from the repositry to the users computer. ")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"USER_ID"})})
@XmlRootElement
public class Download extends EntityBaseBean implements Serializable {

    @Comment("A download is associated with a user.")
    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ICATUser user;

    @Comment("A download has a Entity collection")
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "download")
    private EntityCollection entityCollection;

    @Comment("The preparedID from TopCAT")
    private UUID preparedID;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("The time the download took place.")
    private Date downloadTime;

    @Comment("Ip address of where the download took place.")
    private InetAddress ipAddress;

    @Comment("The method of download.")
    private String method;

    @Comment("The size of the download.")
    private Long size;

    public Download() {

    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setUser(ICATUser user) {
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

    public Long getSize() {
        return size;
    }

    public ICATUser getUser() {
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
