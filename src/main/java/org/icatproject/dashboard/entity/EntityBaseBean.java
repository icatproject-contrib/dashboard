package org.icatproject.dashboard.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import javax.xml.bind.annotation.XmlTransient;


import org.icatproject.dashboard.exceptions.DashboardException;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityBaseBean implements Serializable {

	
	
	

	@Column(name = "CREATE_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	protected Date createTime;

	/** Count of this entity and its descendants */
	@XmlTransient
	@Transient
	private long descendantCount = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seq")
        @SequenceGenerator(
                name="seq",
                sequenceName="id_seq"
                
        )
	protected Long id;
	

	@Column(name = "MOD_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	protected Date modTime;
        
       
	
	/**
	 * Gets the createTime of this entity.
	 * 
	 * @return the modTime
	 */
	public Date getCreateTime() {
		return this.createTime;
	}

	public Long getId() {
		return id;
	}
	


	
	

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setId(Long id) {
		this.id = id;
	}      
	

	public void setModTime(Date modTime) {
		this.modTime = modTime;
	}
        
        public void preparePersist() throws DashboardException {
		this.id = null;		
		
		Date now = null;
		if (createTime == null) {
			now = new Date();
			createTime = now;
		}
		if (modTime == null) {
			if (now == null) {
				now = new Date();
			}
			modTime = now;
		}
        }

	

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + id;
	}

	
}
