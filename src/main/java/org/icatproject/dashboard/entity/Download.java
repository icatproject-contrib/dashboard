/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

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

@Comment("A download is the process of saving entities from the repositry to the users computer. ")
@SuppressWarnings("serial")
@Entity
@NamedQueries({
    @NamedQuery(name="Download.methods",
                query="SELECT d.method, count(d.method) FROM Download d WHERE d.downloadStart > :startDate AND d.downloadEnd <=:endDate GROUP BY d.method "),  
    @NamedQuery(name="Download.frequency",
                query="SELECT d.downloadStart, d.downloadEnd FROM Download d "
                    + "WHERE (d.downloadStart BETWEEN :startDate AND :endDate OR d.downloadEnd BETWEEN :startDate AND :endDate) OR (:startDate < d.downloadStart AND :endDate > d.downloadEnd)"),
    @NamedQuery(name="Download.bandwidth",
                query="SELECT d.downloadStart, d.downloadEnd, d.bandwidth FROM Download d "
                    + "WHERE (d.downloadStart BETWEEN :startDate AND :endDate OR d.downloadEnd BETWEEN :startDate AND :endDate) OR (:startDate < d.downloadStart AND :endDate > d.downloadEnd)"),
})
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
    
    @Comment("The duration of the download in milliseconds")
    private long duriation;

    public Download() {

    }

    public void setDuriation(long duriation) {
        this.duriation = duriation;
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

    public long getDuriation() {
        return duriation;
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
