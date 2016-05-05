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
  
    @NamedQuery(name="Download.method.types",
                query="SELECT DISTINCT(d.method) from Download d WHERE d.method IS NOT NULL")
    
   
})
public class Download extends EntityBaseBean implements Serializable {

    @Comment("A download is associated with a user.")
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ICATUser user;

    @Comment("A download has a Entity collection")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "download")    
    private List<DownloadEntity> downloadEntities;
    
    @Comment("A download has a collection of entity ages")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "download") 
    private List<DownloadEntityAge> downloadEntityAges;
    
    @Comment("A download is associated with a geolocation")
    @JoinColumn(name="GEOLOCATION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private GeoLocation location;

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
    
    @Comment("The current status of the download e.g. preparing,started,failed,completed")
    private String status;
    
    @Comment("The transferID of the download to help identify its uniqueness in the IDS calls.")
    private long transferID;

    public Download() {

    }

    public Download(ICATUser user, List<DownloadEntity> downloadEntities, List<DownloadEntityAge> downloadEntityAges, GeoLocation location, String preparedID, Date downloadStart, Date downloadEnd, String method, Long downloadSize, double bandwidth, long duriation, String status, long transferID) {
        this.user = user;
        this.downloadEntities = downloadEntities;
        this.downloadEntityAges = downloadEntityAges;
        this.location = location;
        this.preparedID = preparedID;
        this.downloadStart = downloadStart;
        this.downloadEnd = downloadEnd;
        this.method = method;
        this.downloadSize = downloadSize;
        this.bandwidth = bandwidth;
        this.duriation = duriation;
        this.status = status;
        this.transferID = transferID;
    }
    //Constructor used for testing
    public Download(GeoLocation location,ICATUser user, String preparedID, Date downloadStart, Date downloadEnd, String method, Long downloadSize, double bandwidth, long duriation, String status, long transferID) {
        this.user = user;
        this.location = location;
        this.preparedID = preparedID;
        this.downloadStart = downloadStart;
        this.downloadEnd = downloadEnd;
        this.method = method;
        this.downloadSize = downloadSize;
        this.bandwidth = bandwidth;
        this.duriation = duriation;
        this.status = status;
        this.transferID = transferID;
    }
    

    public long getTransferID() {
        return transferID;
    }

    public void setTransferID(long transferID) {
        this.transferID = transferID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
    
    public void setDownloadEntityAges(List<DownloadEntityAge> downloadEntityAges) {
        this.downloadEntityAges = downloadEntityAges;
    }

    public void setDuriation(long duriation) {
        this.duriation = duriation;
    }

    public void setLocation(GeoLocation location) {
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

    public List<DownloadEntityAge> getDownloadEntityAges() {
        return downloadEntityAges;
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

    public GeoLocation getLocation() {
        return location;
    }

    public double getBandwidth() {
        return bandwidth;
    }
    
    
    

    public String getMethod() {
        return method;
    }

}
