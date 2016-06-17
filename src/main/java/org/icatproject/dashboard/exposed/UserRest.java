/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import static org.icatproject.dashboard.utility.RestUtility.convertMapToJSON;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import static org.icatproject.dashboard.utility.RestUtility.createPrePopulatedLongMap;
import static org.icatproject.dashboard.utility.RestUtility.createPrePopulatedHashSetMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Stateless
@LocalBean
@Path("user")
public class UserRest {

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    /**
     * Gets the name and what they are currently doing of users that are logged in.
     *
     * @param sessionID for authentication
     * @return a JSONArray in the format of [{"name":"uows\/1049128","fullName":"Dr Mantid Test-Account","operation":"login"}]
     * 
     * @throws BadRequestException     
     * @throws NotImplementedException
     * @throws AuthenticationException
     * @throws InternalException
     * @throws NotFoundException
    
     * 
     * @statuscode 200 To indicate success
     */
    @GET
    @Path("logged")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsersLogInfo(@QueryParam("sessionID") String sessionID) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }
        List<String[]> users;

        JSONArray ary = new JSONArray();       
        
        users = manager.createNamedQuery("Users.LoggedIn").getResultList();
        
        HashSet functionalAccounts = properties.getFunctionalAccounts();
                
        if (users.size() > 0) {

            for (Object[] user : users) {

                String name = (String) user[1];  
                
                if(!functionalAccounts.contains(name)){
                
                    String currentOperation = getLatestOperation(name);

                    JSONObject obj = new JSONObject();
                    obj.put("fullName", user[0]);
                    obj.put("name", name);               
                    obj.put("operation", currentOperation);

                    ary.add(obj);
                }
            }
        }

        return ary.toString();
    }
    
    /**
     * Retrieves the unique login frequency for each day between the dates provided. 
     * Will either be for all users if none is specified or the login frequency
     * of a specified user.
     * @param sessionID for authentication.
     * @param startDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @return a JSONArray of JSONObjects in the form of [{"date":"2016-06-07","number":5},{"date":"2016-06-08","number":5},{"date":"2016-06-09","number":0},{"date":"2016-06-10","number":6},{"date":"2016-06-11","number":6},{"date":"2016-06-12","number":5},{"date":"2016-06-13","number":8}]
     * 
     * @throws BadRequestException     
     * @throws NotImplementedException
     * @throws AuthenticationException
     * @throws InternalException
     * @throws NotFoundException
    
     * 
     * @statuscode 200 To indicate success
     */
    @GET
    @Path("logged/frequency/unique")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUniqueLoginFrequency(@QueryParam("sessionID") String sessionID,                                   
                                    @QueryParam("startDate") String startDate,
                                    @QueryParam("endDate") String endDate) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }
        
        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));
        
        LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate();

        TreeMap<LocalDate, Long> loginDates = createPrePopulatedLongMap(startRange, endRange);
        TreeMap<LocalDate, HashSet> userNameDates = createPrePopulatedHashSetMap(startRange, endRange);  
        
        HashSet functionalAccounts = properties.getFunctionalAccounts();
                
        List<Object[]> result = getLoggedUserCount(start,end,"");
        
        for(Object[] day : result){
            LocalDate collectionDate = convertToLocalDate((Date) day[0]);
            String userName = (String) day[1];
            
            if(!functionalAccounts.contains(userName)){
            
                HashSet userSet = userNameDates.get(collectionDate);

                if(!userSet.contains(userName)){
                    Long value  = loginDates.get(collectionDate);
                    loginDates.put(collectionDate, value+=1);
                    userSet.add(userName);
                    userNameDates.put(collectionDate,userSet);
                }
            }
        }
        
        return convertMapToJSON(loginDates).toJSONString();
    }
    
     /**
     * Retrieves the login frequency for each day between the dates provided. 
     * Will either be for all users if none is specified or the login frequency
     * of a specified user.
     * @param sessionID for authentication.
     * @param name unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param startDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @return a JSONArray of JSONObjects in the form of [{"date":"2016-06-07","number":2966},{"date":"2016-06-08","number":813}]
     * 
     * @throws BadRequestException     
     * @throws NotImplementedException
     * @throws AuthenticationException
     * @throws InternalException
     * @throws NotFoundException
    
     * 
     * @statuscode 200 To indicate success
     */
    @GET
    @Path("logged/frequency")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLoginFrequency(@QueryParam("sessionID") String sessionID,                                   
                                    @QueryParam("startDate") String startDate,
                                    @QueryParam("endDate") String endDate,
                                    @QueryParam("name") String name) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }
        
        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));
        
        LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate();

        TreeMap<LocalDate, Long> loginDates = createPrePopulatedLongMap(startRange, endRange);
       
        List<Object[]> result = getLoggedUserCount(start,end,name);
        
        for(Object[] day : result){
            LocalDate collectionDate = convertToLocalDate((Date) day[0]);             
           
            Long value  = loginDates.get(collectionDate);
            loginDates.put(collectionDate, value+=1);
            
        }             

        return convertMapToJSON(loginDates).toJSONString();
    }
   
    
    
    /**
     * Returns the geolocation of currently logged in users.
     *
     * @param sessionID for authentication.
     * @param name in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * 
     * @return The current location of the user in the format of [{"number":1,"city":"Swindon (Polaris House)","countryCode":"United Kingdom","latitude":51.567,"isp":"Science and Technology Facilites Council","longitude":-1.78472}]
     * 
     * @throws BadRequestException     
     * @throws NotImplementedException
     * @throws AuthenticationException
     * @throws InternalException
     * @throws NotFoundException
    
     * 
     * @statuscode 200 To indicate success
     */
    @GET
    @Path("location")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserLocation(@QueryParam("sessionID") String sessionID,
            @QueryParam("name") String name) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Query logQuery = manager.createQuery("SELECT log.id FROM ICATLog log JOIN log.user user WHERE user.name=:name ORDER BY log.logTime desc");
        logQuery.setParameter("name", name);
        logQuery.setMaxResults(1);

        Long logId = (Long) logQuery.getSingleResult();

        GeoLocation geoLocation = getLogLocation(logId);

        JSONArray resultArray = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("number", 1);
        obj.put("city", geoLocation.getCity());
        obj.put("longitude", geoLocation.getLongitude());
        obj.put("latitude", geoLocation.getLatitude());
        obj.put("countryCode", geoLocation.getCountryCode());
        obj.put("isp", geoLocation.getIsp());
        resultArray.add(obj);

        return resultArray.toJSONString();

    }
    

    /**
     * Gets the users most recent operation.
     *
     * @param name of the user
     * @return the most recent operation
     */
    private String getLatestOperation(String name) {

        String operation;

        //First check if the users is downloading or preparing a downloading.            
        Query query = manager.createQuery("SELECT download.status FROM Download download JOIN download.user user WHERE (download.status='preparing' OR download.status='inProgress') AND user.name= :name ORDER BY download.createTime desc");
        query.setParameter("name", name);
        query.setMaxResults(1);

        List<Object> downloadResult = query.getResultList();

        //If the user isn't downloading anything then we need to look into the log table.
        if (downloadResult.isEmpty()) {
            Query logQuery = manager.createQuery("SELECT log.operation FROM ICATLog log JOIN log.user user WHERE user.name= :name ORDER BY log.logTime desc");
            logQuery.setParameter("name", name);
            logQuery.setMaxResults(1);

            operation = (String) logQuery.getSingleResult();

        } else {

            operation = "Download (" + (String) downloadResult.get(0) + ")";

        }

        return operation;

    }

    /**
     * Gets the geolocation of an ICATLog.
     *
     * @param logId the id of the log.
     * @return A geoLocation object of where the Log took place.
     */
    private GeoLocation getLogLocation(Long logId) {

        String locationQuery = "SELECT location from GeoLocation location JOIN location.logs log WHERE log.id='" + logId + "'";       

        List<Object> geoLocation = manager.createQuery(locationQuery).getResultList();        

        return (GeoLocation)geoLocation.get(0);

    }
    
    
    private List<Object[]> getLoggedUserCount(Date start, Date end, String name){
        
        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<ICATLog> icatLog = query.from(ICATLog.class);    
        Join<ICATLog, ICATUser> icatUserJoin = icatLog.join("user");
        
        Predicate betweenStartEnd = cb.between(icatLog.<Date>get("logTime"), start, end);
        Predicate operationPredicate = cb.equal(icatLog.get("operation"), "login");
        
        Predicate finalPredicate;
         
        
         if (!("undefined".equals(name)) && !(("").equals(name))) {            
             Predicate usernamePredicate = cb.equal(icatUserJoin.get("name"),name);
             finalPredicate = cb.and(betweenStartEnd,operationPredicate,usernamePredicate);
         }else{
             finalPredicate = cb.and(betweenStartEnd,operationPredicate);
         }
        
        
        query.multiselect(icatLog.<Date>get("logTime"),icatUserJoin.get("name"));
        
        query.where(finalPredicate);
        
                
        return manager.createQuery(query).getResultList();
        
    }

}
