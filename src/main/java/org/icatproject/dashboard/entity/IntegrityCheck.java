/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


@Comment("The overal data intergrity of Entity Count.")
@Entity
@XmlRootElement
public class IntegrityCheck extends EntityBaseBean implements Serializable {
    
    @Comment("The date the check was checked agaisnt.")
    @Column(unique=true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date checkDate;
    
    @Enumerated(EnumType.STRING)
    @Comment("The type of collection that was carried out.")
    private CollectionType collectionType;
    
    @Comment("If the collection completed.")
    private Boolean passed;
    
    public IntegrityCheck(){
        
    }

    public Date getDate() {
        return checkDate;
    }

    public CollectionType getCollectionType() {
        return collectionType;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setDate(Date date) {
        this.checkDate = date;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
    
    
}
