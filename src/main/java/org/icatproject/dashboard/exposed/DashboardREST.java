/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import static java.time.temporal.TemporalQueries.zone;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
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
import org.icatproject.dashboard.entity.DownloadLocation;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.ForbiddenException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Stateless
@LocalBean
@Path("/v1")
public class DashboardREST {
    
    private String icatURL;
        
    @EJB
    EntityBeanManager beanManager;
    
    @EJB 
    PropsManager properties;
    
    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
     
    private static final Logger logger = LoggerFactory.getLogger(DashboardREST.class);
    
    
        @PostConstruct
        public void init(){
            icatURL=properties.getICATUrl();
        }
        
        @GET
        @Path("icat/cycles")
        @Produces(MediaType.APPLICATION_JSON)
        public String getCycles(){
            return null;
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
            else{             
                 obj.put("Users ", "0");
            }
           
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
            
            Date sDate = dateFormat.parse(startDate);
            Date eDate = dateFormat.parse(endDate);
            
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
            if(!"[]".equals(auth)){
                sessionID = beanManager.login(user, 120, manager);
                
                obj.put("sessionID", sessionID);
                return obj.toString();
            }
            
            throw new ForbiddenException("Access Denied");
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
         * Calculates the amount of downloads that occurred over the provided period.
         * @param sessionID For authentication
         * @param startUnixEpoch Start time in a Unix timestamp.
         * @param endUnixEpoch End time in a Unix timestamp.
         * @return A JSON array of JSON objects with each day between the provided times
         * @throws DashboardException 
         */
        @GET
        @Path("download/frequency")
        @Produces(MediaType.APPLICATION_JSON)
        public String getDownloadFrequency(@QueryParam("sessionID")String sessionID,
                                @QueryParam("startDate")String startUnixEpoch,
                                @QueryParam("endDate")String endUnixEpoch) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }              
           
         
            Date start = new Date(Long.valueOf(startUnixEpoch));
            Date end = new Date(Long.valueOf(endUnixEpoch));
            
            
            LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startUnixEpoch)).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endUnixEpoch)).atZone(ZoneId.systemDefault()).toLocalDate();   
            
            TreeMap<LocalDate,Long> downloadDates = RestUtility.createPrePopulatedMap(startRange, endRange);          
            

            List<Object[]> downloads = new ArrayList();            
            
            downloads = manager.createNamedQuery("Download.frequency").setParameter("startDate", start).setParameter("endDate", end).getResultList();
            
            for(Object[] download: downloads){               
                            
                LocalDate downloadBeginning = RestUtility.convertToLocalDate((Date)download[0]);
                LocalDate downloadEnd = RestUtility.convertToLocalDate((Date)download[1]);
                
                //Bring the download date up to the requested start date.
                while(downloadBeginning.isBefore(startRange)){
                    downloadBeginning = downloadBeginning.plusDays(1);                       
                        
                    }
                
                while((!downloadBeginning.isAfter(endRange))&&(!downloadBeginning.isAfter(downloadEnd))){
                    Long currentTotal = downloadDates.get(downloadBeginning);
                    downloadDates.put(downloadBeginning,currentTotal+=1);
                    downloadBeginning = downloadBeginning.plusDays(1);
                }
            }        
            
            
                     
            
            return  RestUtility.convertMapToJSON(downloadDates).toJSONString();

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
                                @QueryParam("startDate")String startUnixEpoch,
                                @QueryParam("endDate")String endUnixEpoch) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }            
           
           
            
            Date start = new Date(Long.valueOf(startUnixEpoch));
            Date end = new Date(Long.valueOf(endUnixEpoch));

            JSONObject obj = new JSONObject();
            JSONArray ary = new JSONArray();            
            
            List<Object[]> methods = new ArrayList();     
            
            methods = manager.createNamedQuery("Download.methods").setParameter("startDate", start).setParameter("endDate", end).getResultList();
            
            
            if(methods.size()==0){
               obj.put("amount", 1);
               obj.put("method", "No Downloads");              
               ary.add(obj);
               return ary.toString();
            }
                        
            for(int i=0;i<methods.size();i++){   
               obj = new JSONObject();
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

        /***
         * Returns the bandwidth of downloads within the provided dates. 
         * @param sessionID SessionID to authenticate.
         * @param startUnixEpoch The start time as a unix millisecond timestamp.         
         * @param endUnixEpoch The end time as unix millisecond timestamp.
         * @return JSON object includes the start and end dates, download ID and bandwidth.
         * @throws DashboardException 
         */
        @GET
        @Path("download/bandwidth")
        @Produces(MediaType.APPLICATION_JSON)
        public String getBandwidth(@QueryParam("sessionID")String sessionID,
                                   @QueryParam("startDate")String startUnixEpoch,
                                   @QueryParam("endDate")String endUnixEpoch) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");           }                       
           
          
            Date start = new Date(Long.valueOf(startUnixEpoch));
            Date end = new Date(Long.valueOf(endUnixEpoch));           
                       
             
            JSONArray container = new JSONArray();
           
            List<Object[]> downloads = new ArrayList();            
            
            downloads = manager.createNamedQuery("Download.bandwidth").setParameter("startDate", start).setParameter("endDate", end).getResultList();           
            
            
            for(Object[] download: downloads){
                
                JSONObject downloadData = new JSONObject();
                
                downloadData.put("startDate", dateTimeFormat.format(download[0]));
                downloadData.put("endDate",dateTimeFormat.format(download[1]));
                downloadData.put("bandwidth", download[2].toString());
                downloadData.put("id",download[3].toString());              
                
               container.add(downloadData);                               
            }
            
            return container.toJSONString();
        }
        

        /**
         * Calculates the amount of data that was downloaded over a set period of time.
         * @param sessionID To authenticate
         * @param startUnixEpoch Start time in unix epoch millisecond.
         * @param endUnixEpoch End time in unix epoch millisecond.
         * @return A JSONArray of each date between the start and end and how much
         * data was downloaded.
         * @throws DashboardException Issues with accessing the data from the database. 
         */
        @GET 
        @Path("download/size")
        @Produces(MediaType.APPLICATION_JSON)
        public String getSize(@QueryParam("sessionID")String sessionID,
                              @QueryParam("startDate")String startUnixEpoch,
                              @QueryParam("endDate")String endUnixEpoch) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }                
         
            Date start = new Date(Long.valueOf(startUnixEpoch));
            Date end = new Date(Long.valueOf(endUnixEpoch));
            
            
            LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startUnixEpoch)).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endUnixEpoch)).atZone(ZoneId.systemDefault()).toLocalDate();   
            
            DownloadSizeProcessor downloadHelper = new DownloadSizeProcessor(startRange,endRange);         

            List<Object[]> downloadList = new ArrayList();            
            
            downloadList = manager.createNamedQuery("Download.size").setParameter("startDate", start).setParameter("endDate", end).getResultList();
            
            TreeMap<LocalDate,Long> downloadDates = downloadHelper.calculateDataDownloaded(downloadList);      
            
            return RestUtility.convertMapToJSON(downloadDates).toJSONString();
        }
        
        /**
         * Gets the most frequently downloaded entities in order of the most frequent.
         * @param sessionID For authentication
         * @param limit How many entities (descending) to be returned. Default (All are returned)
         * @return The name of the entity and how many times it has been downloaded up to the limit desired.
         * @throws DashboardException Error collecting the data from the database.
         */
        @GET
        @Path("download/entities/frequency")
        @Produces(MediaType.APPLICATION_JSON)
        public String getFrequencyOfEntites(@QueryParam("sessionID")String sessionID,
                                            @QueryParam("limit")long limit) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }                        

            JSONObject obj = new JSONObject();
            JSONArray ary = new JSONArray();
            
            List<Object[]> entityCount = new ArrayList();
           
            
            entityCount  = manager.createNamedQuery("DownloadEntity.frequency").getResultList();
            
            for(int i=0;i<entityCount.size();i++){
                
            }
            
            
            
            return "PLACEHOLDER";
        }
        
        @GET 
        @Path("download/location/global")
         @Produces(MediaType.APPLICATION_JSON)
        public String getDownloadGlobalLocations( @QueryParam("sessionID")String sessionID,
                                                  @QueryParam("startDate")String startUnixEpoch,
                                                  @QueryParam("endDate")String endUnixEpoch) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }              
                    
            Date start = new Date(Long.valueOf(startUnixEpoch));
            Date end = new Date(Long.valueOf(endUnixEpoch));       
            
            List<Object[]> downloadList = new ArrayList();            
            
            downloadList = manager.createNamedQuery("DownloadLocation.global").setParameter("startDate", start).setParameter("endDate", end).getResultList();
            
            JSONArray resultArray = new JSONArray();
            
            for(Object[] download: downloadList){
                JSONObject obj = new JSONObject();
                obj.put("countryCode",download[0]);
                obj.put("amount",download[1]);
             
                
                resultArray.add(obj);
                
            }
            
            
            return resultArray.toJSONString();
        }
        
        @GET
        @Path("download/location/")
        @Produces(MediaType.APPLICATION_JSON)
        public String getDownloadLocations( @QueryParam("sessionID")String sessionID,
                                            @QueryParam("startDate")String startUnixEpoch,
                                            @QueryParam("endDate")String endUnixEpoch) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }              
           
         
            Date start = new Date(Long.valueOf(startUnixEpoch));
            Date end = new Date(Long.valueOf(endUnixEpoch));
            
            
            
            List<Object[]> downloadList = new ArrayList();            
            
            downloadList = manager.createNamedQuery("Users.download.location").setParameter("startDate", start).setParameter("endDate", end).getResultList();
            
            JSONArray resultArray = new JSONArray();
            
            for(Object[] download: downloadList){
                JSONObject obj = new JSONObject();
                obj.put("name",download[0]);
                obj.put("downloadID",download[2]);
                obj.put("longitude",((DownloadLocation) download[1]).getLongitude());
                obj.put("latitude", ((DownloadLocation) download[1]).getLatitude());
                resultArray.add(obj);
                
            }
            
            
            return resultArray.toJSONString();
        }
            

        
        @GET
        @Path("/ping")
        @Produces(MediaType.TEXT_PLAIN)
        public String ping(){
            return "The Dashboard is doing fine!";
        }
        


    }	

