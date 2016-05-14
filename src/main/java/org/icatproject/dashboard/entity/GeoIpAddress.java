/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Entity used to hold corresponding ip address to 
 * corresponding GeoLocations. This will help prevent
 * a large amount of calls to the GeoTool API 
 */
@Comment("Entity used to hold corresponding ip address to corresponding GeoLocations. This will help prevent a large amount of calls to the GeoTool API. ")
@SuppressWarnings("serial")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "IPADDRESS"}) })
@Entity
@XmlRootElement
public class GeoIpAddress extends EntityBaseBean {
    
    @Comment("A GeoIpAddress is associated with a GeoLocation")
    @JoinColumn(name = "GEOLOCATION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private GeoLocation location;    
    
    @Comment("Ip Address")
    private String ipAddress;
    
    public GeoIpAddress(){
        
    }
    
    public GeoIpAddress(GeoLocation location, String ipAddress) {
        this.location = location;
        this.ipAddress = ipAddress;
    }
    
        public GeoLocation getGeoLocation() {
        return location;
    }

    public void setGeoLocation(GeoLocation location) {
        this.location = location;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    
    
    
}
