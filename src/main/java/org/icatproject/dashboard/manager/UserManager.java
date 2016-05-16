/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.namespace.QName;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.icatproject.IcatException_Exception;
import org.icatproject.User;
import org.icatproject.dashboard.collector.DataCollector;
import org.slf4j.LoggerFactory;



@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class UserManager  {
    
    @EJB
    private PropsManager prop;
 
    @EJB
    private IcatDataManager session;
     
    protected ICAT icat;
    protected String sessionID; 
    
    @EJB
    private EntityBeanManager beanManager;
    
    @PersistenceContext(unitName="dashboard")
    private EntityManager manager;
       

    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DataCollector.class);
    
  
    
    @PostConstruct
    public void init(){       
        icat = createICATLink();
        sessionID = session.getSessionID();
    }
    
    
    /**
     * Creates an ICATservice object which can be used to communicate with an ICAT.
     * @return An ICAT object.
     */
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
            LOG.error("Issue creating WSDL link with the ICAT "+ex);
        }
        
       
        return icat;
        
    }
  
    /**
     * Overloaded insertUser method to allow MDB to inject into this class and add new users that appear.
     * @param name Unique name of the user in the ICAT.
     * @return the created user.
     */
    public ICATUser insertUser(String name){
        String query = "SELECT u from User u WHERE u.name= '"+name+"'";             
        List<Object> icatUser;
        
        ICATUser dashBoardUser = new ICATUser(); 
        
        try {
            icatUser = icat.search(sessionID,query);     
            //Haven't found the user in the ICAT. Try our best with what we have.
            if(icatUser.isEmpty()){
                dashBoardUser.setName(name);
                dashBoardUser.setFullName(name);

            }else{
                User user = (User)icatUser.get(0);
                dashBoardUser.setUserICATID(user.getId());
                dashBoardUser.setFullName(user.getFullName());
                dashBoardUser.setName(user.getName());

            }       
        
        
            insertUser(dashBoardUser);
        
        } catch (IcatException_Exception ex) {
            LOG.error("Issue searching for user "+name+" "+ex);
        }
        
        return dashBoardUser;
    }
    /**
     * Inserts a user to the Dashboard database.
     * @param user the ICAT user to insert
     */
    public void insertUser(ICATUser user){
        try {
            beanManager.create(user, manager);
        } catch (DashboardException ex) {
            LOG.error("Issue inserting user into the Dashboard "+ex);
        }
    
    }
    
    
    
   
    
    
}
