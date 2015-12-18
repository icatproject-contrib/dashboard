/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

@Comment("A download is the process of saving entities from the repositry to the users computer. ")
@SuppressWarnings("serial")
@Entity
@NamedQueries({
    @NamedQuery(name="Download.methods",
                query="SELECT d.method, count(d.method) FROM Download d GROUP BY d.method"),      
    
})
@XmlRootElement
public class Download extends EntityBaseBean implements Serializable {

    @Comment("A download is associated with a user.")
    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ICATUser user;

    @Comment("A download has a Entity collection")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "download")    
    private List<DownloadEntity> downloadEntities;
    
    @Comment("A download is associated with a geolocation")
    @JoinColumn(name="DOWNLOADLOCATION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private DownloadLocation location;

    @Comment("The preparedID from TopCAT")
    private String preparedID;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("The time the download started.")
    private Date downloadStart;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("The time the download ended")
    private Date downloadEnd;
    @Comment("The method of download.")
    private String method;

    @Comment("The size of the download.")
    private Long downloadSize;
    
    @Comment("The bandwidth of a download. Bytes per second")
    private double bandwidth;

    public Download() {

    }

    public void setLocation(DownloadLocation location) {
        this.location = location;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }    
   
    public void setUser(ICATUser user) {
        this.user = user;
    }

    public void setDownloadEntities(List<DownloadEntity> downloadEntities) {
        this.downloadEntities = downloadEntities;
    }

    public void setPreparedID(String preparedID) {
        this.preparedID = preparedID;
    }
  

    public void setDownloadStart(Date downloadStart) {
        this.downloadStart = downloadStart;
    }

    public void setDownloadEnd(Date downloadEnd) {
        this.downloadEnd = downloadEnd;
    }

    public void setDownloadSize(Long downloadSize) {
        this.downloadSize = downloadSize;
    }    
    

    public void setMethod(String method) {
        this.method = method;
    }

    public ICATUser getUser() {
        return user;
    }

    public List<DownloadEntity> getDownloadEntities() {
        return downloadEntities;
    }

    public String getPreparedID() {
        return preparedID;
    }

    public Date getDownloadStart() {
        return downloadStart;
    }

    public Date getDownloadEnd() {
        return downloadEnd;
    }

    public Long getDownloadSize() {
        return downloadSize;
    }

    public DownloadLocation getLocation() {
        return location;
    }

    public double getBandwidth() {
        return bandwidth;
    }
    
    
    

    public String getMethod() {
        return method;
    }

}
