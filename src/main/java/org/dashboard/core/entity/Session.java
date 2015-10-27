/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.entity;

import org.dashboard.core.manager.DashboardException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



@SuppressWarnings("serial")
@Entity
@Table(name = "SESSION_")
@NamedQuery(name = "Session.DeleteExpired", query = "DELETE FROM Session s WHERE s.expireDateTime < CURRENT_TIMESTAMP")
public class Session implements Serializable {

	public final static String DELETE_EXPIRED = "Session.DeleteExpired";

	@Id
	private String id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date expireDateTime;

	private String userName;

	// Needed by JPA
	public Session() {
	}

	public Session(String userName, int lifetimeMinutes) {
		this.id = UUID.randomUUID().toString();
		this.userName = userName;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, lifetimeMinutes);
		this.expireDateTime = cal.getTime();
	}

	public String getUserName() throws DashboardException {
		if (expireDateTime.before(new Date()))
			throw new DashboardException(DashboardException.DashboardExceptionType.SESSION, "Session id:" + id
					+ " has expired");
		return userName;
	}

	public String toString() {
		return userName + (expireDateTime.before(new Date()) ? "expired" : "valid");
	}

	public double getRemainingMinutes() throws DashboardException{
		long millis = expireDateTime.getTime() - java.lang.System.currentTimeMillis();
		if (millis < 0) {
			throw new DashboardException(DashboardException.DashboardExceptionType.SESSION, "Session id:" + id
					+ " has expired");
		}
		return millis / 60000.0;
	}

	public String getId() {
		return id;
	}

	public void refresh(int lifetimeMinutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, lifetimeMinutes);
		this.expireDateTime = cal.getTime();
	}

}

