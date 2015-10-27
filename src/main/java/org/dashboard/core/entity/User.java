/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Comment("A user that operates within the ICAT family. They can have downloads or be part of queries.")


@Table(uniqueConstraints ={ @UniqueConstraint(columnNames ={"USER_ICAT_ID"})})
@Entity
public class User extends EntityBaseBean implements Serializable {
    
    @Comment("A user can have downloads.")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Download> downloads = new ArrayList<Download>();
    
    @Comment("A user can perform queries.")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Query> queries = new ArrayList<Query>();
    
    @Comment("A user can either be logged in or out.")
    private boolean logged;
    
    @Comment("The users ID that can be matched in the ICAT")
    @Column(name="USER_ICAT_ID", nullable = false)
    private Long userICATID;    
    
    @Comment("Name of the user")
    private String name;
    
     @Comment("Ip address of the user the last time they used the ICAT.")
    private InetAddress ipAddress;
    
     @Comment("When they last logged in")
    private Date lastLoggedIn;
    
    public User(){
        
    }
    
    public void setUserICATID(Long userICATID) {
        this.userICATID = userICATID;
    }

    public Long getUserICATID() {
        return userICATID;
    }

    public void setDownloads(List<Download> downloads) {
        this.downloads = downloads;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public boolean isLogged() {
        return logged;
    }

    public String getName() {
        return name;
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    } 
      
    public List<Download> getDownloads(){
        return downloads;
    }
    
   
}
