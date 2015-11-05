/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.manager;

import java.util.List;
import java.util.logging.Level;
import org.dashboard.core.entity.Session;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;
import org.dashboard.core.entity.EntityBaseBean;
import org.dashboard.core.manager.DashboardException.DashboardExceptionType;



@Stateless(name="EntityBeanManager", mappedName="ejb/EntityBeanManager")
@TransactionManagement(TransactionManagementType.BEAN)
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
    public void logout(String sessionId, EntityManager manager) throws DashboardException {
		logger.debug("logout for sessionId " + sessionId);
		try {			
			try {				
				Session session = getSession(sessionId, manager);
				manager.remove(session);
				manager.flush();
				
				logger.debug("Session " + session.getId() + " removed.");
				
			} catch (DashboardException e) {
				
				if (e.getType() == DashboardExceptionType.SESSION) {
					throw e;
				} else {
					throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, e.getClass() + " "
							+ e.getMessage());
				}
			} catch (Exception e) {
				throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
		} catch (SecurityException e) {
			throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "SecurityException" + e.getMessage());		
		} catch (RuntimeException e) {
			throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}
    
    
    public Long create(EntityBaseBean bean, EntityManager manager,UserTransaction userTransaction ) throws DashboardException{
        logger.info("Creating: "+bean.getClass().getSimpleName());
        try{
            userTransaction.begin();
            bean.preparePersist(manager,false);
            manager.persist(bean);            
            manager.flush();      
            userTransaction.commit();
            long beanId = bean.getId();
            logger.info("Created :"+bean.getClass().getSimpleName()+" with id: "+beanId);            
            
            return beanId;
        }
        catch (EntityExistsException e) {				
            throw new DashboardException(DashboardException.DashboardExceptionType.OBJECT_ALREADY_EXISTS, e.getMessage());
        } catch (NotSupportedException ex) {
            java.util.logging.Logger.getLogger(EntityBeanManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SystemException ex) {
            java.util.logging.Logger.getLogger(EntityBeanManager.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (Throwable e) {
            logger.trace("Transaction rolled back for creation of " + bean + " because of " + e.getClass() + " "
						+ e.getMessage());
        }
            try {
                userTransaction.rollback();
            } catch (IllegalStateException ex) {
                java.util.logging.Logger.getLogger(EntityBeanManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                java.util.logging.Logger.getLogger(EntityBeanManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SystemException ex) {
                java.util.logging.Logger.getLogger(EntityBeanManager.class.getName()).log(Level.SEVERE, null, ex);
            }
				
          return new Long(0);  
    }
    
    public Boolean delete(EntityBaseBean bean, EntityManager manager) throws DashboardException{
        logger.info("Deleting: "+bean.getClass().getSimpleName());
        try{
            EntityBaseBean beanManaged = find(bean, manager);
            manager.remove(beanManaged);
	    manager.flush();
            logger.info("Deleted: "+bean.getClass().getSimpleName());
            return true;
        }catch (IllegalStateException e) {
                throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
        }catch (SecurityException e) {
                throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "SecurityException" + e.getMessage());
        }
        
    }
    
    private EntityBaseBean find(EntityBaseBean bean, EntityManager manager) throws DashboardException {
		Long primaryKey = bean.getId();
		Class<? extends EntityBaseBean> entityClass = bean.getClass();
		if (primaryKey == null) {
			throw new DashboardException(DashboardException.DashboardExceptionType.NO_SUCH_OBJECT_FOUND, entityClass.getSimpleName()
					+ " has null primary key.");
		}
		EntityBaseBean object = null;
		try {
			object = manager.find(entityClass, primaryKey);
		} catch (Throwable e) {
			throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, "Unexpected DB response " + e);
		}

		if (object == null) {
			throw new DashboardException(DashboardException.DashboardExceptionType.NO_SUCH_OBJECT_FOUND, entityClass.getSimpleName()
					+ "[id:" + primaryKey + "] not found.");
		}
		return object;
    }
    
    
    public List<Object> search(String queryString, EntityManager manager){
        logger.info("Performing query: "+queryString);
        
        Query query = manager.createQuery(queryString);
        
        return query.getResultList();          
        
             
    }
    
    
    
    public void refresh(String sessionId, int lifetimeMinutes, EntityManager manager) throws DashboardException{
            logger.info("Refreshing session: "+sessionId);
            try{
                Session session = getSession(sessionId, manager);
                session.refresh(lifetimeMinutes);
            }
            catch (DashboardException e) {			
				
                if (e.getType() == DashboardExceptionType.SESSION) {
                        throw e;
                } else {
                        throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, e.getClass() + " "
                                        + e.getMessage());
                }
        } catch (Exception e) {
                throw new DashboardException(DashboardException.DashboardExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
        }
    }
				
    
    private Session getSession(String sessionId, EntityManager manager) throws DashboardException {
		Session session = null;
		if (sessionId == null || sessionId.equals("")) {
			throw new DashboardException(DashboardException.DashboardExceptionType.SESSION, "Session Id cannot be null or empty.");
		}
		session = (Session) manager.find(Session.class, sessionId);
		if (session == null) {
			throw new DashboardException(DashboardException.DashboardExceptionType.SESSION, "Unable to find user by sessionid: "
					+ sessionId);
		}
		return session;
	}
   
}
 