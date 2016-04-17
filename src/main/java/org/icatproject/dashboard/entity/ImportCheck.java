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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;


@Comment("The overal data intergrity of Entity Count.")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "CHECKDATE","PASSED" }) })
@Entity
@XmlRootElement
public class ImportCheck extends EntityBaseBean implements Serializable {
    
    @Comment("The date the check was checked agaisnt.")
    @Column(unique=true, nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date checkDate;
    
        
    @Comment("If the collection completed.")
    @Column( nullable = false)
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