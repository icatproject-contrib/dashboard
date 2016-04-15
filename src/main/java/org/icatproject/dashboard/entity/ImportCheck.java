/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


@Comment("The overal data intergrity of Entity Count.")
@Entity
@XmlRootElement
public class ImportCheck extends EntityBaseBean implements Serializable {
    
    @Comment("The date the check was checked agaisnt.")
    @Column(unique=true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date checkDate;
    
        
    @Comment("If the collection completed.")
    private Boolean passed;
    
    public ImportCheck(){
        
    }

    public ImportCheck(Date checkDate, Boolean passed) {
        this.checkDate = checkDate;
        this.passed = passed;
    }
    
    

    public Date getDate() {
        return checkDate;
    }

    
    public Boolean getPassed() {
        return passed;
    }

    public void setDate(Date date) {
        this.checkDate = date;
    }

   

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
    
    
}