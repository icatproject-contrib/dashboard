/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

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
import javax.ws.rs.ApplicationPath;
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
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
    
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    
     
    private static final Logger logger = LoggerFactory.getLogger(DashboardREST.class);
    
    
        @PostConstruct
        public void init(){
            icatURL=properties.getICATUrl();
        }
        
        
        /**
         * Gets the name of users which are currently logged into the ICAT
         * @param sessionID Session ID
         * @return Names of users currently logged into ICAT.
         * @throws DashboardException 
         */
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
        
        /**
         * Gets the amount of downloads performed by a user the over a specific time period.
         * If only the sessionID 
         * 
         * @param sessionID Session ID
         * @param userName The unique name of the user. 
         * @param startDate Start date to check from. Format yyyyMMdd.
         * @param endDate End Date to check up to. Format yyyyMMdd.
         * @return JSON string containing the names of the users with the amount of downloads they have performed.
         * @throws ParseException Incorrect date format provided.
         * @throws BadRequestException Incorrect SessionID provided or not one provided at all.
         */
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
         * Post login to the dashboard. Authentication is done via the ICAT.
         * @param login Login object containing the authenticator, username and password.
         * @return Session ID.
         * @throws URISyntaxException Incorrect ICAT URL provided.
         * @throws IcatException Issue authenticating with the ICAT.
         * @throws BadRequestException Username, authenticator or password is missing.
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
            throw new org.icatproject.dashboard.exceptions.IcatException(ex.getMessage());
        }            
        

        }

        
        /**
         * Deletes the sessionID in the dashboard to log the user out.
         * @param sessionID SessionID to be deleted.
         * @return Logout Successful message.
         * @throws DashboardException Unable to find the sessionID.
         */
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
        
        /**
         * Gets the routes used by downloads e.g. Globus. 
         * @param sessionID SessionID for authentication.
         * @QueryParam startDate Start point for downloads.
         * @QueryParam endDate end points for downloads.
         * @return The type of route and the amount of times used over the set period.
         * @throws BadRequestException Incorrect date formats or a valid sessionID.
         */
        @GET
        @Path("download/route")
        @Produces(MediaType.APPLICATION_JSON)
        public String getRoutes(@QueryParam("sessionID")String sessionID,
                                @DefaultValue("19500101") @QueryParam("startDate")String startDate,
                                @DefaultValue("21000101") @QueryParam("endDate")String endDate) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }
            
            Date start;
            Date end;
            
            try {
                start = format.parse(startDate);
                end = format.parse(endDate);
            } catch (ParseException ex) {
                throw new BadRequestException("Invalid date format should be ddMMyyyy");
            }

            JSONObject obj = new JSONObject();
            JSONArray ary = new JSONArray();
            
            List<Object[]> methods = new ArrayList();
            Map methodCount = new HashMap();
            
            methods = manager.createNamedQuery("Download.methods").setParameter("startDate", start).setParameter("endDate", end).getResultList();
            
            
            if(methods.size()==0){
               obj.put("amount", 1);
               obj.put("method", "No Downloads");              
               ary.add(obj);
               return ary.toString();
            }
                        
            for(int i=0;i<methods.size();i++){              
               String method = methods.get(i)[0].toString();
               long amount = (long)methods.get(i)[1];  
               obj.put("amount", amount);
               obj.put("method",method);              
               ary.add(obj);
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
        @Path("download/bandwidth")
        @Produces(MediaType.APPLICATION_JSON)
        public String getBandwidth(@QueryParam("sessionID")String sessionID,
                                    @QueryParam("Username")String userName,
                                    @DefaultValue("19500101") @QueryParam("startDate")String startDate,
                                    @DefaultValue("21000101") @QueryParam("endDate")String endDate) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }
            
            Date start;
            Date end;
            
            try {
                start = format.parse(startDate);
                end = format.parse(endDate);
            } catch (ParseException ex) {
                throw new BadRequestException("Invalid date format should be ddMMyyyy");
            }
            return new String("0");
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
        
        @GET
        @Path("ping")
        @Produces(MediaType.TEXT_PLAIN)
        public String ping(){
            return "The Dashboard is doing fine!";
        }
        


    }	

