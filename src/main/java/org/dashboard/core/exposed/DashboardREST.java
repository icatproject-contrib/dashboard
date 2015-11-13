/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.exposed;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;
import org.dashboard.core.manager.DashboardException;
import org.dashboard.core.manager.DashboardException.DashboardExceptionType;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.PropsManager;
import org.dashboard.core.entity.ICATUser;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.JSONWriter;
import org.json.simple.JSONObject;


@Stateless
@Path("/")
public class DashboardREST {
    
    private String icatURL;
        
    @EJB
    EntityBeanManager beanManager;
    
    @EJB 
    PropsManager properties;
    
    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;
    
     
    private static Logger logger = Logger.getLogger(DashboardREST.class);
    
    
        @PostConstruct
        public void init(){
            icatURL=properties.getICATUrl();
        }
   
	@GET
	@Path("userInfo/login/{loggedIn}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUsersLogInfo(@PathParam("loggedIn")String loggedIn,
                                      @QueryParam("sessionID")String sessionID) throws DashboardException{
            
            if(sessionID == null){
                throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "sessionID must be provided");
            }
            List<String> users = null;
            JSONObject obj = new JSONObject();
            String loginMessage = null;
           
            if(loggedIn.equals("1")){
                users = manager.createNamedQuery("Users.LoggedIn").getResultList();
                loginMessage = "Logged in";
            }
            else if(loggedIn.equals("0")){
                users = manager.createNamedQuery("Users.LoggedOut").getResultList();
                loginMessage = "Logged out";
            }
            
            if(users.size()>0){
                for(int i=0;i<users.size();i++){
                    obj.put(users.get(i), loginMessage);                
                }
                return obj.toString();
            }          
             
            obj.put("Users ", "0");
           
            return obj.toString();
	}
	
	
	@GET
	@Path("userInfo/location/{userName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLocation(@PathParam("userName")String userName, @QueryParam("sessionID")String sessionID){
            
		return null;
	
	
        }
   
        @GET
        @Path("test")
        public String test(){
            return "ok";
        }
       
        
        @POST
        @Path("session/login")
        @Consumes(MediaType.APPLICATION_JSON)
        public String login(Login login) throws DashboardException, URISyntaxException, IcatException{
            
            if(login.getAuthenticator() ==null){
                throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "authenticator type must be provided");
            }
            if(login.getUsername() == null){
                throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "username must be provided");
            }
            if(login.getPassword() == null){
                throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "password must be provided");
            }
            
            ICAT icat = new ICAT(icatURL);
            Map<String, String> credentials = new HashMap<>();
            
            String user;
            String sessionID = null;
            JSONObject obj = new JSONObject();
            String authenticator;
            
            credentials.put("username",login.getUsername());
            credentials.put("password",login.getPassword());
            authenticator=login.getAuthenticator();
            
            Session session = icat.login(authenticator, credentials);
            
            user = session.getUserName();
            String auth = session.search("SELECT u FROM User u JOIN u.userGroups ug JOIN ug.grouping g WHERE u.name='"+user+"' AND g.name='Dashboard'");
            session.logout();
            if(auth!=null){
                sessionID = beanManager.login(user, 120, manager);
                
                obj.put("sessionID ", sessionID);
                return obj.toString();
            }
            
            obj.put("Failed Login","Access Denied");
           
            return obj.toString();
            
        

        }


        @DELETE
        @Path("session/logout")
        public String logut(@QueryParam("sessionID")String sessionID){
        try {
            beanManager.logout(sessionID, manager);
        } catch (DashboardException ex) {
            java.util.logging.Logger.getLogger(DashboardREST.class.getName()).log(Level.SEVERE, null, ex);
            
        }
            return null;
        }


        @GET
        @Path("entity/count")
        @Produces(MediaType.APPLICATION_JSON)
        public String getEntityCount(@QueryParam("sessionID")String sessionID,@QueryParam("EntityType")String type,@QueryParam("startDate")String startDate,@QueryParam("endDate")String endDate){
            return null;

        }

        @GET
        @Path("download/route")
        @Produces(MediaType.APPLICATION_JSON)
        public String getRoutes(){
            return null;

        }

        @GET
        @Path("download/age")
        @Produces(MediaType.APPLICATION_JSON)
        public String getFileAge(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")String startDate,@QueryParam("endDate")String endDate){
            return null;

        }

        @GET
        @Path("download/frequency")
        @Produces(MediaType.APPLICATION_JSON)
        public String getFrequent(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")String startDate,@QueryParam("endDate")String endDate){return null;
      
        }


        @GET 
        @Path("download/size")
        @Produces(MediaType.APPLICATION_JSON)
        public String getSize(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")String startDate,@QueryParam("endDate")String endDate){
            JSONObject obj = new JSONObject();
            obj.put("Test","OK");
            return obj.toString();

        }


    }	

