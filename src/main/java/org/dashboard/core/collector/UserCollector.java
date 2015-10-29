/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.dashboard.core.entity.CollectionType;
import static org.dashboard.core.entity.CollectionType.UserUpdate;
import org.dashboard.core.entity.ICATUser;
import org.dashboard.core.entity.IntegrityCheck;
import org.dashboard.core.manager.DashboardException;
import org.dashboard.core.manager.EntityBeanManager;
import org.icatproject.IcatException_Exception;
import org.icatproject.User;



@Singleton
public class UserCollector extends Collector {
    
    @EJB
    private EntityBeanManager beanManager;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;
       
    
    /**
     * Goes through the set of dates provided and collects the users for each day from ICAT.
     * 
     * @param start date to check from for users.
     * @param end  date to check up to for users.
     */
    public void collectUsers(LocalDate start, LocalDate end){
        List<Object> users = new ArrayList();
        
         while(start.isBefore(end) ){
            try {
                boolean complete = true;
                        
                String query = "SELECT u FROM User U WHERE u.modTime >={ts "+ start.format(format) +" 00:00:00 } AND u.modTime <= {ts "+start.format(format)+" 23:59:59 }";
                users = icat.search(sessionID,query);
                
                for(Object temp : users){
                    User user = (User)temp;
                    
                    ICATUser dashBoardUser = new ICATUser();
                    
                    dashBoardUser.setUserICATID(user.getId());
                    dashBoardUser.setName(user.getFullName());
                    
                    //Failed for the day do not wish to continue
                    if(!(complete = insertUser(dashBoardUser))){                        
                        break;
                    }                    
                }
                if(complete){
                    integerityUpdate(start, true,UserUpdate);
                    start.plusDays(1);
                }
                else{
                    integerityUpdate(start, false,UserUpdate);
                    start.plusDays(1);
                }
                        
            } catch (IcatException_Exception ex) {
                Logger.getLogger(UserCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
    /**
     * Inserts a user to the Dashboard database.
     * @param user the ICAT user to insert
     * @return if it was successfully inserted or not.
     */
    public boolean insertUser(ICATUser user){
        try {
            beanManager.create(user, manager);
        } catch (DashboardException ex) {
            Logger.getLogger(UserCollector.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public void integerityUpdate(LocalDate date, boolean passed, CollectionType type) {
        IntegrityCheck ic = new IntegrityCheck();
        ic.setCollectionType(type);
        ic.setDate(java.sql.Date.valueOf(date));
        ic.setPassed(passed);
        
        try {
            beanManager.create(ic, manager);
        } catch (DashboardException ex) {
            Logger.getLogger(UserCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Inserts a integrity value for the Integrity table. 
     * @param date Date the collection went over.
     * @param passed If it was successful or not.
     * @param type the type of collection e.g. User update or entity count.
     */
    
    
}
