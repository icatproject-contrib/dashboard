/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.manager;

import org.dashboard.core.entity.Session;
import javax.ejb.LocalBean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;



@Stateless(name="EntityBeanManager", mappedName="ejb/EntityBeanManager")
@LocalBean
public class EntityBeanManager {
       
    private boolean log;
    
    private static final Logger logger = Logger.getLogger(EntityBeanManager.class);
    
    public String login(String userName, int lifetimeMinutes, EntityManager manager) throws DashboardException {
        Session session = new Session(userName, lifetimeMinutes);
        try {
			
			try {
				long time = log ? System.currentTimeMillis() : 0;
				manager.persist(session);
				manager.flush();				
				String result = session.getId();
                            logger.debug("Session " + result + " persisted.");				
				return result;
			} catch (Throwable e) {				
				logger.trace("Transaction rolled back for login because of " + e.getClass() + " " + e.getMessage());
				throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "Unexpected DB response "
						+ e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "IllegalStateException " + e.getMessage());
		} catch (SecurityException e) {
			throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "SecurityException " + e.getMessage());
		}
    }
    
    
    public String getTest(){
        return "Working...";
    }
}
 