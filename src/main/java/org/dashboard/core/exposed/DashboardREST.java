/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.exposed;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;
import org.dashboard.core.exceptions.AuthenticationException;
import org.dashboard.core.exceptions.BadRequestException;
import org.dashboard.core.exceptions.DashboardException;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.PropsManager;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONArray;
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
    
    private SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
    
     
    private static Logger logger = Logger.getLogger(DashboardREST.class);
    
    
        @PostConstruct
        public void init(){
            icatURL=properties.getICATUrl();
        }
   
	@GET
	@Path("user/login")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUsersLogInfo(@QueryParam("sessionID")String sessionID) throws DashboardException{
            
            if(sessionID == null){
                throw new BadRequestException("sessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }
            List<String> users = null;
            JSONObject obj = new JSONObject();
            String loginMessage = null;           

            users = manager.createNamedQuery("Users.LoggedIn").getResultList();
            loginMessage = "Logged in";
           
            
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
	@Path("user/location/{userName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLocation(@PathParam("userName")String userName, @QueryParam("sessionID")String sessionID){
            
		return null;
	
	
        }
        
        @GET
        @Path("user/download/frequency")
        @Produces(MediaType.APPLICATION_JSON)
        public String getFrequent(@QueryParam("sessionID")String sessionID,
                                  @QueryParam("Username")String userName,
                                  @DefaultValue("19500101") @QueryParam("startDate")String startDate,
                                  @DefaultValue("21000101") @QueryParam("endDate")String endDate) throws DashboardException, ParseException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }
            
            Date sDate = format.parse(startDate);
            Date eDate = format.parse(endDate);
            
            List<Object[]> downloads = new ArrayList();
            if(userName==null){
                 downloads =  manager.createNamedQuery("Users.DownloadCount").setParameter("startDate", sDate)
                                                                             .setParameter("endDate", eDate)
                                                                              .getResultList();  
                 String dog = "test";
            }
            else{
                downloads = manager.createNamedQuery("Users.DownloadCount.User").setParameter("startDate", sDate)
                                                                                .setParameter("endDate", eDate)
                                                                                .setParameter("name", userName)
                                                                                .getResultList(); 
                String dog = "test";
            }
            return null;
        }
        
         
       
        /**
         * 
         * @param login
         * @return
         * @throws DashboardException
         * @throws URISyntaxException
         * @throws IcatException 
         */
        @POST
        @Path("session/login")
        @Consumes(MediaType.APPLICATION_JSON)
        public String login(Login login) throws DashboardException, URISyntaxException {
            
        try {
            if(login.getAuthenticator() ==null){
                throw new BadRequestException(" authenticator type must be provided");
            }
            if(login.getUsername() == null){
                throw new BadRequestException(" username must be provided");
            }
            if(login.getPassword() == null){
                throw new BadRequestException(" password must be provided");
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
                
                obj.put("sessionID", sessionID);
                return obj.toString();
            }
            
            obj.put("Failed Login","Access Denied");
           
            return obj.toString();
        } catch (IcatException ex) {
            throw new org.dashboard.core.exceptions.IcatException(ex.getMessage());
        }            
        

        }


        @DELETE
        @Path("session/logout")
        public String logut(@QueryParam("sessionID")String sessionID) throws DashboardException{
       
            beanManager.logout(sessionID, manager);        
            JSONObject obj = new JSONObject();
            obj.put("Logout","Successful");
            
            return obj.toString();
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
        public String getRoutes(@QueryParam("sessionID")String sessionID) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }
            JSONObject obj = new JSONObject();
            JSONArray ary = new JSONArray();
            
            List<Object[]> methods = new ArrayList();
            Map methodCount = new HashMap();
            
            methods = manager.createNamedQuery("Download.methods").getResultList();
            if(methods.get(0)==null){
                obj.put("Status:", "There are currently no methods of downloads.");
                return obj.toString();
            }
                        
            for(int i=0;i<methods.size();i++){
               JSONObject o = new JSONObject();
               String method = methods.get(i)[0].toString();
               long amount = (long)methods.get(i)[1];  
               o.put("amount", amount);
               o.put("method",method);
              
               ary.add(o);
            }
                     
            
            return ary.toString();

        }

        @GET
        @Path("download/age")
        @Produces(MediaType.APPLICATION_JSON)
        public String getFileAge(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")String startDate,@QueryParam("endDate")String endDate){
            return null;

        }

        


        @GET 
        @Path("download/size")
        @Produces(MediaType.APPLICATION_JSON)
        public String getSize(@QueryParam("sessionID")String sessionID,
                              @QueryParam("Username")String userName,
                              @DefaultValue("19500101") @QueryParam("startDate")String startDate,
                              @DefaultValue("21000101") @QueryParam("endDate")String endDate) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }
            if(userName==null){
                beanManager.search("SELECT d FROM Download d   ", manager);
            }
            JSONObject obj = new JSONObject();
            obj.put("Test","OK");
            return obj.toString();

        }


    }	

