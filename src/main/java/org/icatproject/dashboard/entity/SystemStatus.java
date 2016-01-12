/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.sql.Time;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;




@Comment("The status of the ICAT family products.")
@Entity
@XmlRootElement
public class SystemStatus extends EntityBaseBean implements Serializable{
    
    @Comment("The System that has had the check.")
    private System system;
    
    @Comment("Time the check took place.")
    private Time time;
    
    @Comment("Status of the system.")
    private Boolean up;
    
    public SystemStatus(){
        
    }

    public System getSystem() {
        return system;
    }

    public Time getTime() {
        return time;
    }

    public Boolean getUp() {
        return up;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public void setUp(Boolean up) {
        this.up = up;
    }
    
    
}
