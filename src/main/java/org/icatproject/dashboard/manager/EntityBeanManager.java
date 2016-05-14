/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.manager;

import org.icatproject.dashboard.exceptions.DashboardException;
import java.util.Date;
import java.util.List;
import org.icatproject.dashboard.entity.Session;

import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.icatproject.dashboard.entity.EntityBaseBean;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.InternalException;
import org.icatproject.dashboard.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Stateless(name = "EntityBeanManager", mappedName = "ejb/EntityBeanManager")
public class EntityBeanManager {

 

    private static final Logger logger = LoggerFactory.getLogger(EntityBeanManager.class);
    
    
    /**
     * Checks to see if the sessionID provided is valid.
     * @param sessionID The sessionID to be checked.
     * @param manager EntityManager to prevent threading issues.
     * @return If the sessionID is valid.
     */
    public boolean checkSessionID(String sessionID, EntityManager manager) {       
        Session session = (Session) manager.find(Session.class, sessionID);        
        return session!=null;
    }
    
    
    /**
     * Logs the user into the dashboard. Creates a new session object and adds it to the database.
     * @param userName The name of the user.
     * @param lifetimeMinutes The length of time they are to be added for.
     * @param manager To prevent threading problems the EntityManager is passed a long.
     * @return The sessionID.
     * @throws DashboardException if it is unable to talk to the database. 
     */
    public String login(String userName, int lifetimeMinutes, EntityManager manager) throws DashboardException {
        Session session = new Session(userName, lifetimeMinutes);
        try {

            try {

               
                manager.persist(session);
                manager.flush();
                String result = session.getId();

                return result;
            } catch (Throwable e) {
                logger.trace("Transaction rolled back for login because of " + e.getClass() + " " + e.getMessage());
                throw new InternalException(e.getMessage());
            }
        } catch (IllegalStateException | SecurityException e) {
            throw new InternalException(e.getMessage());
        }
    }

    /**
     * *
     * Logs the user out of the dashboard. Deletes the session ID from the
     * database.
     *
     * @param sessionId The sessionID to be removed
     * @param manager EntityManager is passed through to prevent threading
     * issues
     * @throws DashboardException The session cannot be found.
     */
    public void logout(String sessionId, EntityManager manager) throws DashboardException {
              
        Session session = getSession(sessionId, manager);
        manager.remove(session);
        manager.flush();
        

           
    }

    /**
     * Persist then flushes the Entity object to the database connected to it.
     *
     * @param bean The entity object that is to be created.
     * @param manager EntityManager object to stop threading problems.
     * @return The ID of the bean
     * @throws DashboardException If the entity exists.
     */
    public Long create(EntityBaseBean bean, EntityManager manager) throws DashboardException {
        logger.info("Creating: " + bean.getClass().getSimpleName());
        try {
            bean.preparePersist();
            manager.persist(bean);
            manager.flush();

            long beanId = bean.getId();
            logger.info("Created :" + bean.getClass().getSimpleName() + " with id: " + beanId);

            return beanId;
        } catch (EntityExistsException e) {
            throw new InternalException(e.getMessage());

        } catch (Throwable e) {
            logger.trace("Transaction rolled back for creation of " + bean + " because of " + e.getClass() + " "
                    + e.getMessage());
        }

        return new Long(0);
    }

    /**
     * Updates the bean provided in the dashboard database.
     * @param bean The bean to be updated.
     * @param manager EntityManager to prevent threading issues.
     */
    public void update(EntityBaseBean bean, EntityManager manager) {
        logger.info("Updating: "+bean.getClass().getSimpleName());
        
        bean.setModTime(new Date());
        manager.merge(bean);
                
        
    }
    /**
     * Gets the entity from the database and deletes.
     * @param bean The bean to be deleted.
     * @param manager EntityManager is provided to prevent threading issues.
     * @return If it was successful or not in deleting the entity.
     * @throws DashboardException If it is unable to delete the entity.
     */
    public Boolean delete(EntityBaseBean bean, EntityManager manager) throws DashboardException {
        logger.info("Deleting: " + bean.getClass().getSimpleName());
        try {
            EntityBaseBean beanManaged = find(bean, manager);
            manager.remove(beanManaged);
            manager.flush();
            logger.info("Deleted: " + bean.getClass().getSimpleName());
            return true;
        } catch (IllegalStateException e) {
            throw new InternalException(e.getMessage());
        } 

    }

    /**
     * Finds the bean inside the dashboard database.
     * @param bean The bean to be found in the database.
     * @param manager Entitymanager to prevent threading issues.
     * @return The EntityBaseBean that was found.
     * @throws DashboardException If it is unable to find the bean.
     */
    private EntityBaseBean find(EntityBaseBean bean, EntityManager manager) throws DashboardException {
        Long primaryKey = bean.getId();
        Class<? extends EntityBaseBean> entityClass = bean.getClass();
        if (primaryKey == null) {
            throw new BadRequestException("No Primary Key Found.");
        }
        EntityBaseBean object = null;
        try {
            object = manager.find(entityClass, primaryKey);
        } catch (Throwable e) {
            throw new InternalException(e.getMessage());
        }

        if (object == null) {
            throw new NotFoundException(bean.getClass().toString()+" cannot be found.");
        }
        return object;
    }

    /**
     * Provides a search function to go over the data in the dashboard.
     * @param queryString The query it's self.     
     * @param manager EntityManager to prevent threading issues.
     * @return A list of objects found.
     */
    public List<Object> search(String queryString, EntityManager manager) throws InternalException {
        logger.info("Performing query: " + queryString);
        Query query = null;
        try {

            query = manager.createQuery(queryString);

        } catch (SecurityException ex ) {
            throw new InternalException(ex.getMessage());
        } 

        return query.getResultList();

    }

    /***
     * Refreshes the sessionID for the user.
     * @param sessionId The sessionID to be refreshed.
     * @param lifetimeMinutes How long the sessionID should be refreshed for.
     * @param manager EntityManager to be prevent threading issues.
     * @throws DashboardException If it is unable to refresh the sessionID
     */
    public void refresh(String sessionId, int lifetimeMinutes, EntityManager manager) throws DashboardException {
   
        Session session = getSession(sessionId, manager);
        session.refresh(lifetimeMinutes);
       
    }

    /**
     * Returns the session object associated with a sessionID.
     * @param sessionId associated with the session Object.
     * @param manager EntityManager to prevent threading issues.
     * @return A session Object.
     * @throws DashboardException If it is unable to find the session. 
     */
    private Session getSession(String sessionId, EntityManager manager) throws DashboardException {
        Session session = null;
        if (sessionId == null || sessionId.equals("")) {
            throw new BadRequestException("Session Id cannot be null or empty.");
        }
        session = (Session) manager.find(Session.class, sessionId);
        if (session == null) {
            throw new AuthenticationException("Unable to find sessionID");
        }
        return session;
    }

}
