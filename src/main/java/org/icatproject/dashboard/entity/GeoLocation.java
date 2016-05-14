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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("A geoLocation of where an activity such as a download took place.")
@NamedQueries({    
    @NamedQuery(name="GeoLocation.check",
                query="SELECT ge FROM GeoLocation ge WHERE ge.longitude = :longitude AND ge.latitude = :latitude"), 
    
    @NamedQuery(name="GeoLocation.ipCheck",
                query="SELECT ge FROM GeoLocation ge JOIN ge.geoIpAddresses geoIp WHERE geoIp.ipAddress= :ipAddress "),
})
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "LONGITUDE", "LATITUDE"}) })
@Entity
public class GeoLocation extends EntityBaseBean {
    
    @Comment("A geolocation has many downloads")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<Download> downloads;
    
    @Comment("A geolocation has many ICATLogs")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<ICATLog> logs;
    
    @Comment("A GeoLocation has a collection of ipAddress associated with it.")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location") 
    private List<GeoIpAddress> geoIpAddresses;
    
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
    
    

    public GeoLocation(double longitude, double latitude, String countryCode, String city, String isp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.countryCode = countryCode;
        this.city = city;
        this.isp = isp;
        
    }
    
    public GeoLocation(){
        
    }

    public List<GeoIpAddress> getGeoIpAddresses() {
        return geoIpAddresses;
    }

    public void setGeoIpAddresses(List<GeoIpAddress> geoIpAddresses) {
        this.geoIpAddresses = geoIpAddresses;
    }
   
    
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
