/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Comment("A Query")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "QUERYID","USER_ID" }) })
public class Query extends EntityBaseBean implements Serializable {
    
    @Comment("The user which performed the query.")
    @JoinColumn(name="USER_ID", nullable = false)
    @ManyToOne(fetch= FetchType.LAZY)
    private ICATUser user;
    
    @Comment("The id of the query in ICAT.")
    private Long queryID;

    
    @Comment("Length of time the query took.")
    private Long Duration;
    
    @Comment("Type of query that took place.")
    private String type;
    
    @Comment("The actual query.")
    private String query;
    
    
    public Query(){
        
    }
    
    public void setQueryID(Long queryID) {
        this.queryID = queryID;
    }

    public Long getQueryID() {
        return queryID;
    }
    
    public void setUser(ICATUser user) {
        this.user = user;
    }

    public void setDuration(Long Duration) {
        this.Duration = Duration;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ICATUser getUser() {
        return user;
    }

    public Long getDuration() {
        return Duration;
    }

    public String getType() {
        return type;
    }

    public String getQuery() {
        return query;
    }
    
    
    
    
}
