/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

@Comment("A download location is the geolocation of where the download took place.")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
public class DownloadLocation extends EntityBaseBean {
    
    @Comment("A geolocation has many downloads")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<Download> downloads;
    
    @Comment("The longtitude of a download")
    private double longitude;
    
    @Comment("The latitude of a download")    
    private double latitude;
    
    @Comment("The hostmachine of the download")
    private String hostMachineName;
    
    @Comment("The ipAddress of location.")
    private String ipAddress;

    public List<Download> getDownloads() {
        return downloads;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getHostMachineName() {
        return hostMachineName;
    }

    public void setDownloads(List<Download> downloads) {
        this.downloads = downloads;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setHostMachineName(String hostMachineName) {
        this.hostMachineName = hostMachineName;
    }
    
    
    
}
