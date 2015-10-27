/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;



@Comment("The collection of entites that can be associated with a download.")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames={"DOWNLOAD_ID"})})
public class EntityCollection extends EntityBaseBean implements Serializable {
    
    @JoinColumn(name="DOWNLOAD_ID",nullable=false)
    @OneToOne(fetch = FetchType.LAZY)
    private Download download;

       
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entityCollection")
    private List<Item> entites = new ArrayList<>();
    
    public EntityCollection(){
        
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    public void setEntites(List<Item> entites) {
        this.entites = entites;
    }
    
    public Download getDownload() {
        return download;
    }

    public List<Item> getEntites() {
        return entites;
    }
    
    
    
    
}
