package org.dashboard.core.manager;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.dashboard.core.entity.Session;


@Singleton
public class SessionManager {

	private static final Logger logger = Logger.getLogger(SessionManager.class);

	@PersistenceContext(unitName="dashboard")
	private EntityManager manager;

	// Run every hour
	@Schedule(hour = "*")
	public void removeExpiredSessions() {
		try {
			int n = manager.createNamedQuery(Session.DELETE_EXPIRED).executeUpdate();
			logger.debug(n + " sessions were removed");
		} catch (Throwable e) {
			logger.error(e.getClass() + " " + e.getMessage());
		}
	}
}