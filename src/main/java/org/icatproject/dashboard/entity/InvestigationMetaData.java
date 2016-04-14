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


@Comment("This entity records meta information on an investigation during a day.")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
public class InvestigationMetaData extends EntityBaseBean implements Serializable{
    
    
    @Comment("A short name for the investigation")
    @Column(name = "NAME", nullable = false)
    private String name;

    @Comment("Full title of the investigation")
    @Column(nullable = false)
    private String title;
    
    @Comment("The investigation visit id")
    private String visitId;
    
    @Comment("The number of datasets associated with this instrument on a specific day")
    private Long datasetCount;
    
    @Comment("The number of datafiles associated with this instrument on a specific day")
    private Long datafileCount;
    
    @Comment("The volume of datafiles associated with this instrument on a specific day")
    private Long datafileVolume;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Comment("Time of the meta collection.")
    private Date collectionDate;
    
    @Comment("Id in the ICAT on the investigation associated with this meta data")
    private long investigationId;
    
   
    
    public InvestigationMetaData(){}

    public InvestigationMetaData(String name, String title, String visitId, Long datasetCount, Long datafileCount, Long datafileVolume, Date collectionDate, long investigationId) {
        this.name = name;
        this.title = title;
        this.visitId = visitId;
        this.datasetCount = datasetCount;
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

      
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }  

   
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }    


    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public Long getDatasetCount() {
        return datasetCount;
    }

    public void setDatasetCount(Long datasetCount) {
        this.datasetCount = datasetCount;
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
