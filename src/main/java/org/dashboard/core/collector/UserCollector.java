/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import org.dashboard.core.entity.CollectionType;
import static org.dashboard.core.entity.CollectionType.UserUpdate;
import org.dashboard.core.entity.ICATUser;
import org.dashboard.core.entity.IntegrityCheck;
import org.dashboard.core.manager.DashboardException;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.ICATSessionManager;
import org.dashboard.core.manager.PropsManager;
import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.icatproject.IcatException_Exception;
import org.icatproject.User;



@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class UserCollector  {
    
    @EJB
    private PropsManager prop;
 
    @EJB
    private ICATSessionManager session;
     
    protected ICAT icat;
    protected String sessionID; 
    
    @EJB
    private EntityBeanManager beanManager;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;
       
    DateTimeFormatter format;
    
  
    
    @PostConstruct
    public void init(){
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        icat = createICATLink();
        sessionID = session.getSessionID();
    }
    
    /**
     * Goes through the set of dates provided and collects the users for each day from ICAT.
     * 
     * @param start date to check from for users.
     * @param end  date to check up to for users.
     */
    public void collectUsers(LocalDate start, LocalDate end){
        List<Object> users = new ArrayList();
        
         while(!start.isAfter(end)){
            try {
                boolean complete = true;
                        
                String query = "SELECT u FROM User U WHERE u.modTime >={ts "+ start.format(format) +" 00:00:00 } AND u.modTime <= {ts "+start.format(format)+" 23:59:59 }";
                users = icat.search(sessionID,query);
                
                for(Object temp : users){
                    User user = (User)temp;
                    
                    ICATUser dashBoardUser = new ICATUser();
                    
                    dashBoardUser.setUserICATID(user.getId());
                    dashBoardUser.setFullName(user.getFullName());
                    dashBoardUser.setName(user.getName());
                    
                    //Failed for the day do not wish to continue
                    if(!(complete = insertUser(dashBoardUser))){                        
                        break;
                    }                    
                }
                if(complete){
                    integerityUpdate(start, true,UserUpdate);
                    start = start.plusDays(1);
                }
                else{
                    integerityUpdate(start, false,UserUpdate);
                    start = start.plusDays(1);
                }
                        
            } catch (IcatException_Exception ex) {
                Logger.getLogger(UserCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
     public ICAT createICATLink(){
        ICAT icat = null;
         try {
            URL hostUrl;
            
            hostUrl = new URL(prop.getICATUrl());
            URL icatUrl = new URL(hostUrl, "ICATService/ICAT?wsdl");
            QName qName = new QName("http://icatproject.org", "ICATService");
            ICATService service = new ICATService(icatUrl, qName);
            icat = service.getICATPort();            
                                        
                    
        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        return icat;
        
    }
    /**
     * Overloaded insertUser method to allow MDB to inject into this class and add new users that appear.
     * @param name Unique name of the user in the ICAT.
     */
    public ICATUser insertUser(String name){
        String query = "SELECT u from User u WHERE u.name= '"+name+"'";
        User u = null;        
        
        try {
            List<Object> user = icat.search(sessionID,query);
            u=(User)user.get(0);
        } catch (IcatException_Exception ex) {
            Logger.getLogger(UserCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
        ICATUser dashBoardUser = new ICATUser();
        
        dashBoardUser.setUserICATID(u.getId());
        dashBoardUser.setFullName(u.getFullName());
        dashBoardUser.setName(u.getName());
        
        insertUser(dashBoardUser);
        
        return dashBoardUser;
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
    
     /**
     * Inserts a integrity value for the Integrity table. 
     * @param date Date the collection went over.
     * @param passed If it was successful or not.
     * @param type the type of collection e.g. User update or entity count.
     */

   
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
    
    
   
    
    
}
