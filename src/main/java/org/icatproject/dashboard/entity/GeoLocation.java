/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Comment("A geoLocation of where an activity such as a download took place.")
@NamedQueries({    
    @NamedQuery(name="GeoLocation.check",
                query="SELECT ge FROM GeoLocation ge WHERE ge.longitude = :longitude AND ge.latitude = :latitude"), 
})
@Entity
public class GeoLocation extends EntityBaseBean {
    
    @Comment("A geolocation has many downloads")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<Download> downloads;
    
    @Comment("A geolocation has many ICATLogs")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<ICATLog> logs;
    
    @Comment("The longtitude of a download")
    private double longitude;
    
    @Comment("The latitude of a download")    
    private double latitude;
    
    @Comment("The country code of the location.")
    private String countryCode;
    
    @Comment("The city of the location.")
    private String city;
    
    @Comment("The ISP at this location.")
    private String isp;

    
    
    public List<ICATLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ICATLog> logs) {
        this.logs = logs;
    }   
    

    public String getIsp() {
        return isp;
    }

    
    public String getCountryCode() {
        return countryCode;
    }

    public String getCity() {
        return city;
    }   
    

    public List<Download> getDownloads() {
        return downloads;
    }    

    
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }   

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setCity(String city) {
        this.city = city;
    }    
    

    public void setDownloads(List<Download> downloads) {
        this.downloads = downloads;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    
    
    
}
