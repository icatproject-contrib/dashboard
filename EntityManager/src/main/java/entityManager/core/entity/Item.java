/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entityManager.core.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Comment("This is a item that has been downloaded via the IDS. It belongs to an Entity Collection")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints ={ @UniqueConstraint(columnNames = {"ENTITYCOLLECTION_ID,ENTITY_TYPE,ENTITY_ID"})})
public class Item extends EntityBaseBean implements Serializable{
    
    @Comment("The Collection the Item belongs to.")
    @JoinColumn(name="ENTITYCOLLECTION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityCollection entityCollection;
    
    
    @Comment("Name of the Item.")
    private String name;
    
    @Comment("Size of the Item in bytes.")
    private Long size;
    
    @Comment("Time of creation of the Item in the ICAT.")
    private Date ICATcreationTime;
    
    @Comment("Type of Item it is e.g. Investigatio, Datafile, Dataset... etc")
    private String type;
    
    public Item(){
        
    }
    
    
    
}
