/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.DownloadEntityAge;
import org.icatproject.dashboard.entity.DownloadLocation;
import org.icatproject.dashboard.entity.ICATUser;
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
         * Calculates the number of downloads that occurred over the provided period.
         * @param sessionID For authentication
         * @param startUnixEpoch Start time in a Unix timestamp.
         * @param endUnixEpoch End time in a Unix timestamp.
         * @param userName Unique name of the user. Corresponds to name in the ICAT user table.
         * @param method the method of download 
         * @return A JSON array of JSON objects with each day between the provided times
         * @throws DashboardException 
         */
        @GET
        @Path("download/frequency")
        @Produces(MediaType.APPLICATION_JSON)
        public String getDownloadFrequency(@QueryParam("sessionID")String sessionID,
                                @QueryParam("startDate")String startUnixEpoch,
                                @QueryParam("endDate")String endUnixEpoch,
                                @QueryParam("userName")String userName,
                                @QueryParam("method")String method) throws DashboardException{
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
            
             //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<Download> download = query.from(Download.class);
            
                        
            //Get methods and count how many their are.
            query.multiselect(download.get("downloadStart"),download.get("downloadEnd"));            
            
            Predicate finalPredicate = createDownloadPredicate(cb,start,end,download,userName, method);     
            
            query.where(finalPredicate);         
            
            List<Object[]> downloads = manager.createQuery(query).getResultList();
                      
                        
            for(Object[] singleDownload: downloads){               
                            
                LocalDate downloadBeginning = RestUtility.convertToLocalDate((Date)singleDownload[0]);
                LocalDate downloadEnd = RestUtility.convertToLocalDate((Date)singleDownload[1]);
                
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
         * @param startDate Start point for downloads.
         * @param endDate end points for downloads.
         * @param userName name of the user to check against.
         * @return The type of route and the number of times used over the set period.
         * @throws BadRequestException Incorrect date formats or a invalid sessionID.
         */
        @GET
        @Path("download/method")
        @Produces(MediaType.APPLICATION_JSON)
        public String getMethods(@QueryParam("sessionID")String sessionID,
                                 @QueryParam("startDate")String startDate,
                                 @QueryParam("endDate")String endDate,
                                 @QueryParam("userName")String userName) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }  
            
            Date start = new Date(Long.valueOf(startDate));
            Date end = new Date(Long.valueOf(endDate));
                      
            //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<Download> download = query.from(Download.class);
            
                        
            //Get methods and count how many their are.
            query.multiselect(download.get("method"),cb.count(download.get("method")));
            
            
            Predicate finalPredicate = createDownloadPredicate(cb,start,end,download,userName, null);            
            
            
            query.where(finalPredicate);
            
            //Finally group by the method
            query.groupBy(download.get("method"));
            
            List<Object[]> methods = manager.createQuery(query).getResultList();

            
            JSONArray ary = new JSONArray();            
                  
            
               
            for(Object[] result: methods){   
               JSONObject obj = new JSONObject();
               String method = result[0].toString();
               long number = (long)result[1];  
               obj.put("number", number);
               obj.put("method",method);              
               ary.add(obj);
            }
                     
            
            return ary.toJSONString();

        }
        
        /***
         * Gets all the types of download methods used in the ICAT family.
         * @param sessionID To authenticate the user.
         * @return a JSON containing all the different methods of downloads.
         * @throws AuthenticationException Invalid sessionID.
         * @throws BadRequestException No session ID provided.
         */
        @GET
        @Path("download/method/types")
        @Produces(MediaType.APPLICATION_JSON)
        public String getMethods(@QueryParam("sessionID")String sessionID) throws AuthenticationException, BadRequestException{
            
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }  
            
            List<String> methodTypes;
            
            methodTypes =  manager.createNamedQuery("Download.method.types").getResultList(); 
            
            
            JSONArray ary = new JSONArray();            
                       
            for(int i=0;i<methodTypes.size();i++){  
               JSONObject obj = new JSONObject();
               String method = methodTypes.get(i);       
               obj.put("method",method);              
               ary.add(obj);
            }
            
            return ary.toJSONString();
        }

      
        /***
         * Returns the bandwidth of downloads within the provided dates. 
         * @param sessionID SessionID for authentication.
         * @param startDate Start point for downloads.
         * @param endDate end points for downloads.
         * @param userName name of the user to check against.
         * @param method type of download method.
         * @return JSON object includes the start and end dates, download ID and bandwidth.
         * @throws DashboardException 
         */
        @GET
        @Path("download/bandwidth")
        @Produces(MediaType.APPLICATION_JSON)
        public String getBandwidth(@QueryParam("sessionID")String sessionID,
                                   @QueryParam("startDate")String startDate,
                                   @QueryParam("endDate")String endDate,
                                   @QueryParam("userName")String userName,
                                   @QueryParam("method")String method) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");           }                       
           
          
            Date start = new Date(Long.valueOf(startDate));
            Date end = new Date(Long.valueOf(endDate));           
                       
             
            JSONArray container = new JSONArray();
            
            //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<Download> download = query.from(Download.class);
            
                        
            //Get methods and count how many their are.
            query.multiselect(download.get("downloadStart"),download.get("downloadEnd"), download.get("bandwidth"),download.get("id"));
            
            
            Predicate finalPredicate = createDownloadPredicate(cb,start,end,download,userName, method);           
            
            
            query.where(finalPredicate);     
          
            
            List<Object[]> downloads = manager.createQuery(query).getResultList();                
            
            
            for(Object[] singleDownload: downloads){
                
                JSONObject downloadData = new JSONObject();
                
                downloadData.put("startDate", dateTimeFormat.format(singleDownload[0]));
                downloadData.put("endDate",dateTimeFormat.format(singleDownload[1]));
                downloadData.put("bandwidth", singleDownload[2].toString());
                downloadData.put("id",singleDownload[3].toString());              
                
               container.add(downloadData);                               
            }
            
            return container.toJSONString();
        }
        

        /**
         * Calculates the number of data that was downloaded over a set period of time.
         * @param sessionID SessionID for authentication.
         * @param startDate Start point for downloads.
         * @param endDate end points for downloads.
         * @param userName name of the user to check against.
         * @param method type of download method.
         * @return A JSONArray of each date between the start and end and how much
         * data was downloaded.
         * @throws DashboardException Issues with accessing the data from the database. 
         */
        @GET 
        @Path("download/size")
        @Produces(MediaType.APPLICATION_JSON)
        public String getSize(@QueryParam("sessionID")String sessionID,
                              @QueryParam("startDate")String startDate,
                              @QueryParam("endDate")String endDate,
                              @QueryParam("userName")String userName,
                              @QueryParam("method")String method) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }                
         
            Date start = new Date(Long.valueOf(startDate));
            Date end = new Date(Long.valueOf(endDate));
            
            
            LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate();   
            
            DownloadSizeProcessor downloadHelper = new DownloadSizeProcessor(startRange,endRange);         

            //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<Download> download = query.from(Download.class);
            
                        
            //Get methods and count how many their are.
            query.multiselect(download.get("downloadStart"),download.get("downloadEnd"), download.get("downloadSize"));
            
            
            Predicate finalPredicate = createDownloadPredicate(cb,start,end,download,userName, method);          
            
            
            query.where(finalPredicate);     
          
            
            List<Object[]> downloads = manager.createQuery(query).getResultList();                      
            
            
            TreeMap<LocalDate,Long> downloadDates = downloadHelper.calculateDataDownloaded(downloads);      
            
            return RestUtility.convertMapToJSON(downloadDates).toJSONString();
        }
        
        /***
         * Gets the age of files in days of every download within the set parameters.
         * @param sessionID SessionID for authentication.
         * @param startDate Start point for downloads.
         * @param endDate end points for downloads.
         * @param userName name of the user to check against.
         * @param method type of download method.
         * @return a JSONArray of JSONObjects in the format {number:10,age:200}
         * @throws DashboardException Issues with accessing the dashboard database.
         */
        @GET
        @Path("download/entities/age")
        @Produces(MediaType.APPLICATION_JSON)
        public String getEntityAge(@QueryParam("sessionID")String sessionID,
                              @QueryParam("startDate")String startDate,
                              @QueryParam("endDate")String endDate,
                              @QueryParam("userName")String userName,
                              @QueryParam("method")String method) throws DashboardException{
            
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }                
         
            Date start = new Date(Long.valueOf(startDate));
            Date end = new Date(Long.valueOf(endDate));
            
            //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<DownloadEntityAge> downloadEntityAge = query.from(DownloadEntityAge.class);
            
            //Join between downloadEntityAge, downloads and users.
            Join<DownloadEntityAge, Download> downloadJoin = downloadEntityAge.join("download");
            Join<Download, ICATUser> downloadUserJoin = downloadJoin.join("user");
                        
           
            query.multiselect(cb.sum(downloadEntityAge.<Long>get("amount")),downloadEntityAge.get("age"));
            
            
            Predicate startGreater = cb.greaterThan(downloadJoin.<Date>get("downloadStart"), start);
            Predicate endLess = cb.lessThan(downloadJoin.<Date>get("downloadEnd"),end);
            Predicate betweenStart = cb.between(downloadJoin.<Date>get("downloadStart"),start,end);
            Predicate betweenEnd = cb.between(downloadJoin.<Date>get("downloadEnd"),start,end);
            
            Predicate combineBetween = cb.or(betweenStart,betweenEnd);
            Predicate combineGL = cb.and(startGreater,endLess);
            Predicate finalPredicate = cb.or(combineBetween, combineGL);
            
             if(method!=null && !method.equals("undefined")){
                Predicate methodPredicate = cb.equal(downloadJoin.get("method"), method);
                finalPredicate = cb.and(finalPredicate, methodPredicate);
            }
            
            if(userName!=null && !userName.equals("undefined")){                
                Predicate userPredicate = cb.equal( downloadUserJoin.get("name"), userName);  
                finalPredicate = cb.and(finalPredicate,userPredicate);
            } 
            
            query.groupBy(downloadEntityAge.get("age"));
            
            
            query.where(finalPredicate);     
          
            
            List<Object[]> downloads = manager.createQuery(query).getResultList();
            
            
            JSONArray ary = new JSONArray();
            
            for(Object[] download:downloads){
                JSONObject obj = new JSONObject();
                obj.put("number", download[0]);
                obj.put("age", download[1]);
                ary.add(obj);
            }
                              
               
            return ary.toJSONString();
        
        }
        
        /***
         * Gets the number of downloads in each country.
         * @param sessionID SessionID for authentication.
         * @param startDate Start point for downloads.
         * @param endDate end points for downloads.
         * @param userName name of the user to check against.
         * @param method type of download method.
         * @return a JSON String in the format {countryCode:GB, Number:10}
         * @throws DashboardException issue with accessing the database
         */
        @GET 
        @Path("download/location/global")
         @Produces(MediaType.APPLICATION_JSON)
        public String getDownloadGlobalLocations( @QueryParam("sessionID")String sessionID,
                                                  @QueryParam("startDate")String startDate,
                                                  @QueryParam("endDate")String endDate,
                                                  @QueryParam("userName")String userName,
                                                  @QueryParam("method")String method) throws DashboardException{
            
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }              
            
            Date start = new Date(Long.valueOf(startDate));
            Date end = new Date(Long.valueOf(endDate)); 
            
            //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<DownloadLocation> downloadLocation = query.from(DownloadLocation.class);           
            
            //Join between downloads and location.
            Join<Download, DownloadLocation> downloadLocationJoin = downloadLocation.join("downloads");
            
            //Get methods and count how many their are.
            query.multiselect(downloadLocation.get("countryCode"),cb.count(downloadLocation.get("countryCode")));            
            
            Predicate finalPredicate = createDownloadLocationPredicate(cb,start,end,downloadLocation,downloadLocationJoin,userName, method);     
            
            query.where(finalPredicate);  
            
            //Finally group by the method
            query.groupBy(downloadLocation.get("countryCode"));          
            
            List<Object[]> downloadGlobalLocations = manager.createQuery(query).getResultList();
            
           
            
            JSONArray resultArray = new JSONArray();
            
            for(Object[] download: downloadGlobalLocations){
                JSONObject obj = new JSONObject();
                obj.put("countryCode",download[0]);
                obj.put("number",download[1]);            
                
                resultArray.add(obj);
                
            }
            
            
            return resultArray.toJSONString();
        }
        
         /***
         * Gets the number of downloads in each set of longitude and latitude pairs.
         * @param sessionID SessionID for authentication.
         * @param startDate Start point for downloads.
         * @param endDate end points for downloads.
         * @param userName name of the user to check against.
         * @param method type of download method.
         * @return a JSON String in the format {city:Appelton, Number:10, Longitude:20.20, Latitude:-1.34}
         * @throws DashboardException issue with accessing the database
         */
        @GET
        @Path("download/location")
        @Produces(MediaType.APPLICATION_JSON)
        public String getDownloadLocations( @QueryParam("sessionID")String sessionID,
                                            @QueryParam("startDate")String startDate,
                                            @QueryParam("endDate")String endDate,
                                            @QueryParam("userName")String userName,
                                            @QueryParam("method")String method) throws DashboardException{
            if(sessionID==null){
                throw new BadRequestException("A SessionID must be provided");
            }
            if(!(beanManager.checkSessionID(sessionID, manager))){
                throw new AuthenticationException("An invalid sessionID has been provided");
            }              
           
         
            Date start = new Date(Long.valueOf(startDate));
            Date end = new Date(Long.valueOf(endDate));
            
            
            
            //Criteria objects.
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Object[]>  query = cb.createQuery(Object[].class);
            Root<DownloadLocation> downloadLocation = query.from(DownloadLocation.class); 
            
            Join<Download, DownloadLocation> downloadLocationJoin = downloadLocation.join("downloads"); 
            
            Predicate finalPredicate = createDownloadLocationPredicate(cb,  start, end,  downloadLocation, downloadLocationJoin,  userName, method);    
                        
            //Get methods and count how many their are.
            query.multiselect(downloadLocation,cb.count(downloadLocationJoin));            
            
            //Predicate finalPredicate = createDownloadLocationPredicate(cb,start,end,downloadLocation,userName, method);     
            
            query.where(finalPredicate);  
            
            //Finally group by the method
            query.groupBy(downloadLocation);          
            
            List<Object[]> downloadLocalLocations = manager.createQuery(query).getResultList();
            
            JSONArray resultArray = new JSONArray();
            
            for(Object[] download: downloadLocalLocations){
                JSONObject obj = new JSONObject();
                obj.put("number",download[1]);
                obj.put("city",((DownloadLocation) download[0]).getCity());
                obj.put("longitude",((DownloadLocation) download[0]).getLongitude());
                obj.put("latitude", ((DownloadLocation) download[0]).getLatitude());
                resultArray.add(obj);
                
            }
            
            
            return resultArray.toJSONString();
        }
            

        /***
         * Simple ping to see if the dashboard is up and running.
         * @return Message saying it is fine.
         */
        @GET
        @Path("/ping")
        @Produces(MediaType.TEXT_PLAIN)
        public String ping(){
            return "The Dashboard is doing fine!";
        }
        
        /***
         * Creates a predicate that applies a restriction to gather all downloads between the 
         * start and end date and any during those period.
         * @param cb CriteriaBuilder to build the Predicate.
         * @param start Start time of the predicate statement.
         * @param end End time of the predicate statement.
         * @param userName The name of a ICATuser to add to the predicate.
         * @param method The name of a method to add to the predicate.
         * @return a predicate object that contains restrictions to gather all downloads during the start
         * and end date.
         */
        private Predicate createDownloadPredicate(CriteriaBuilder cb, Date start, Date end, Root<Download> download,  String userName, String method){
            
            Predicate startGreater = cb.greaterThan(download.<Date>get("downloadStart"), start);
            Predicate endLess = cb.lessThan(download.<Date>get("downloadEnd"),end);
            Predicate betweenStart = cb.between(download.<Date>get("downloadStart"),start,end);
            Predicate betweenEnd = cb.between(download.<Date>get("downloadEnd"),start,end);
            
            Predicate combineBetween = cb.or(betweenStart,betweenEnd);
            Predicate combineGL = cb.and(startGreater,endLess);
            Predicate finalPredicate = cb.or(combineBetween, combineGL);
            
             if(method!=null && !method.equals("undefined")){
                Predicate methodPredicate = cb.equal(download.get("method"), method);
                finalPredicate = cb.and(finalPredicate, methodPredicate);
            }
            
            if(userName!=null && !userName.equals("undefined")){
                Join<ICATUser, Download> downloadJoin = download.join("user"); 
                Predicate userPredicate = cb.equal(downloadJoin.get("name"), userName);  
                finalPredicate = cb.and(finalPredicate,userPredicate);
            }           
           
            
            return finalPredicate;
            
        }
        
         /***
         * Creates a predicate that applies a restriction to gather all downloadLocations between the 
         * start and end date and any during those period.
         * @param cb CriteriaBuilder to build the Predicate.
         * @param start Start time of the predicate statement.
         * @param end End time of the predicate statement.
         * @param userName The name of a ICATuser to add to the predicate.
         * @param method The name of a method to add to the predicate.
         * @return a predicate object that contains restrictions to gather all downloadLocations during the start
         * and end date.
         */
        private Predicate createDownloadLocationPredicate(CriteriaBuilder cb, Date start, Date end, Root<DownloadLocation> downloadLocation, Join<Download, DownloadLocation> downloadLocationJoin, String userName, String method){
            
            Predicate startGreater = cb.greaterThan(downloadLocationJoin.<Date>get("downloadStart"), start);
            Predicate endLess = cb.lessThan(downloadLocationJoin.<Date>get("downloadEnd"),end);
            Predicate betweenStart = cb.between(downloadLocationJoin.<Date>get("downloadStart"),start,end);
            Predicate betweenEnd = cb.between(downloadLocationJoin.<Date>get("downloadEnd"),start,end);
            
            Predicate combineBetween = cb.or(betweenStart,betweenEnd);
            Predicate combineGL = cb.and(startGreater,endLess);
            Predicate finalPredicate = cb.or(combineBetween, combineGL);
            
             if(method!=null && !method.equals("undefined")){
                Predicate methodPredicate = cb.equal(downloadLocationJoin.get("method"), method);
                finalPredicate = cb.and(finalPredicate, methodPredicate);
            }
            
            if(userName!=null && !userName.equals("undefined")){
                Join<ICATUser, Download> downloadUserJoin = downloadLocationJoin.join("user"); 
                Predicate userPredicate = cb.equal(downloadUserJoin.get("name"), userName);  
                finalPredicate = cb.and(finalPredicate,userPredicate);
            }           
            
           
           
            
            return finalPredicate;
        }

       

    }	

