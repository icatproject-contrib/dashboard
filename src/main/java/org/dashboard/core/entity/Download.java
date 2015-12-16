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
import javax.persistence.OneToOne;
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
    private UUID preparedID;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("The time the download started.")
    private Date downloadStart;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("The time the download ended")
    private Date downloadEnd;

    @Comment("Ip address of where the download took place.")
    private InetAddress ipAddress;

    @Comment("The method of download.")
    private String method;

    @Comment("The size of the download.")
    private Long downloadSize;

    public Download() {

    }

   
    public void setUser(ICATUser user) {
        this.user = user;
    }

    public void setDownloadEnties(List<DownloadEntity> downloadEntities) {
        this.downloadEntities = downloadEntities;
    }

    public void setPreparedID(UUID preparedID) {
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
    
    
   

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
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

    public UUID getPreparedID() {
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
    
    
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getMethod() {
        return method;
    }

}
