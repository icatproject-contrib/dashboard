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


@Comment("This entity records meta information on an investigation during a day.")
@SuppressWarnings("serial")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATIONID", "COLLECTIONDATE" }) })
@Entity
@XmlRootElement
public class InvestigationMetaData extends EntityBaseBean implements Serializable{
    
    
       
    @Comment("The number of datafiles associated with this instrument on a specific day")
    private Long datafileCount;
    
    @Comment("The volume of datafiles associated with this instrument on a specific day")
    private Long datafileVolume;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Time of the meta collection.")
    @Column( nullable = false)
    private Date collectionDate;
    
    @Comment("Id in the ICAT on the investigation associated with this meta data")
    @Column( nullable = false)
    private long investigationId;
    
   
    
    public InvestigationMetaData(){}

    public InvestigationMetaData(Long datafileCount, Long datafileVolume, Date collectionDate, long investigationId) {
        this.datafileCount = datafileCount;
        this.datafileVolume = datafileVolume;
        this.collectionDate = collectionDate;
        this.investigationId = investigationId;
    }

    

    

    public long getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(long investigationId) {
        this.investigationId = investigationId;
    }

     
    public Long getDatafileCount() {
        return datafileCount;
    }

    public void setDatafileCount(Long datafileCount) {
        this.datafileCount = datafileCount;
    }

    public Long getDatafileVolume() {
        return datafileVolume;
    }

    public void setDatafileVolume(Long datafileVolume) {
        this.datafileVolume = datafileVolume;
    }

    public Date getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(Date collectionDate) {
        this.collectionDate = collectionDate;
    }
    
    
    
    
}
