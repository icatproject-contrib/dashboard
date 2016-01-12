package org.icatproject.dashboard.manager;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.icatproject.dashboard.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class DashboardSessionManager {

	private static final Logger logger = LoggerFactory.getLogger(DashboardSessionManager.class);

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