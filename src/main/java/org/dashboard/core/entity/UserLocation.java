
package org.dashboard.core.entity;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

@Comment("A user location is the geolocation of where the user currently is or was.")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
public class UserLocation  extends EntityBaseBean {
    
    @Comment("A geolocation has many users")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")    
    private List<ICATUser> users;
    
    @Comment("The longtitude of a download")
    private double longitude;
    
    @Comment("The latitude of a download")    
    private double latitude;
    
    @Comment("The hostmachine of the download")
    private String hostMachineName;   

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getHostMachineName() {
        return hostMachineName;
    }

    public List<ICATUser> getUsers() {
        return users;
    }
    
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setHostMachineName(String hostMachineName) {
        this.hostMachineName = hostMachineName;
    }

    public void setUsers(List<ICATUser> users) {
        this.users = users;
    }
    
    
    
}
