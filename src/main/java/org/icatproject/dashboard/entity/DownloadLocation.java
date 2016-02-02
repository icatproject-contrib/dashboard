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
import javax.xml.bind.annotation.XmlRootElement;

@Comment("A download location is the geolocation of where the download took place.")
@NamedQueries({
    @NamedQuery(name="DownloadLocation.global",
                query="SELECT dl.countryCode, count(dl.countryCode) FROM DownloadLocation dl JOIN dl.downloads d WHERE d.downloadStart > :startDate AND d.downloadEnd <=:endDate AND dl.countryCode IS NOT NULL GROUP BY dl.countryCode "), 
    @NamedQuery(name="DownloadLocation.check",
                query="SELECT dl FROM DownloadLocation dl WHERE dl.longitude = :longitude AND dl.latitude = :latitude"), 
})
@Entity
public class DownloadLocation extends EntityBaseBean {
    
    @Comment("A geolocation has many downloads")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<Download> downloads;
    
    @Comment("The longtitude of a download")
    private double longitude;
    
    @Comment("The latitude of a download")    
    private double latitude;    
    
    
    @Comment("The country code of the location.")
    private String countryCode;
    
    @Comment("The city of the location.")
    private String city;

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

    
    
    
}
