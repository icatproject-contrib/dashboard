/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;  
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Comment("Summary or abstract")
    @Column(length = 4000)
    private String summary;

    @Comment("Full title of the investigation")
    @Column(nullable = false)
    private String title;
    
    @Comment("Facility associated with this investigation")
    private String facility;
    
    @Comment("The investigation type")
    private String type;
    
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

    public InvestigationMetaData(String name, Date startDate, Date endDate, String summary, String title, String facility, String type, String visitId, Long datasetCount, Long datafileCount, Long datafileVolume, Date collectionDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.summary = summary;
        this.title = title;
        this.facility = facility;
        this.type = type;
        this.visitId = visitId;
        this.datasetCount = datasetCount;
        this.datafileCount = datafileCount;
        this.datafileVolume = datafileVolume;
        this.collectionDate = collectionDate;
    }

    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
