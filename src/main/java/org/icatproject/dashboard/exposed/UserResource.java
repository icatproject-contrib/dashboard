/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
import org.icatproject.dashboard.consumers.GeoTool;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.DownloadEntityAge;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import static org.icatproject.dashboard.utility.RestUtility.convertMapToJSON;
import static org.icatproject.dashboard.utility.RestUtility.convertResultsToJson;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDateTime;
import static org.icatproject.dashboard.utility.RestUtility.createPrePopulatedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Stateless
@LocalBean
@Path("user")
public class UserResource {

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    /**
     * Gets details on users which are currently logged into the ICAT
     *
     * @param sessionID Session ID
     * @return a JSONString containing the users name, fullName, login duration
     * and current activity.
     * @throws DashboardException
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

        if (users.size() > 0) {

            for (Object[] user : users) {

                String name = (String) user[1];
                Duration loggedTime = getLoggedinTime(name);
                String currentOperation = getLatestOperation(name);

                JSONObject obj = new JSONObject();
                obj.put("fullName", user[0]);
                obj.put("name", name);
                obj.put("loggedTime", loggedTime.toMinutes());
                obj.put("operation", currentOperation);

                ary.add(obj);
            }
        }

        return ary.toString();
    }
    
    /**
     * Retrieves the login frequency for each day between the dates provided. 
     * Will either be for all users if none is specified or the login frequency
     * of a specified user.
     * @param sessionID for authentication.
     * @param username of a specific user.
     * @param startDate to search from.
     * @param endDate to search from.
     * @return a JSONArray of JSONObjects containing date and frequency.
     * @throws DashboardException issue verifying the user.
     */
    @GET
    @Path("logged/frequency")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLoginFrequency(@QueryParam("sessionID") String sessionID,                                   
                                    @QueryParam("startDate") String startDate,
                                    @QueryParam("endDate") String endDate,
                                    @QueryParam("username") String username) throws DashboardException {

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

        TreeMap<LocalDate, Long> loginDates = createPrePopulatedMap(startRange, endRange);

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<ICATLog> icatLog = query.from(ICATLog.class);
        
       
        
        Predicate betweenStartEnd = cb.between(icatLog.<Date>get("logTime"), start, end);
        Predicate operationPredicate = cb.equal(icatLog.get("operation"), "login");
        
        Predicate finalPredicate;
        
         if (null!=username) {
             Join<ICATLog, ICATUser> icatUserJoin = icatLog.join("user");
             Predicate usernamePredicate = cb.equal(icatUserJoin.get("name"),username);
             finalPredicate = cb.and(betweenStartEnd,operationPredicate,usernamePredicate);
         }else{
             finalPredicate = cb.and(betweenStartEnd,operationPredicate);
         }
        
        
        
        query.multiselect(icatLog.<Date>get("logTime"));
        
        query.where(finalPredicate);
        
        List<Object> result = manager.createQuery(query).getResultList();
        
        for(Object day : result){
            LocalDate collectionDate = convertToLocalDate((Date) day);
            
            if(loginDates.containsKey(collectionDate)){
                Long value  = loginDates.get(collectionDate);
                loginDates.put(collectionDate, value+=1);
            }
            else{
                loginDates.put(collectionDate, new Long(1));
            }
            
            
        }   
       

        return convertMapToJSON(loginDates).toJSONString();
    }
    
    
    /**
     * Returns the geo location of currently logged in users.
     *
     * @param sessionID SessionID for authentication.
     * @param name name of the user to check against.
     * @return All the information on downloads.
     * @throws BadRequestException Incorrect date formats or a invalid
     * sessionID.
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

        Query logQuery = manager.createQuery("SELECT log.id FROM ICATLog log JOIN log.user user WHERE user.name= :name ORDER BY log.logTime desc");
        logQuery.setParameter("name", name);
        logQuery.setMaxResults(1);

        Long logId = (Long) logQuery.getSingleResult();

        GeoLocation geoLocation = getLogLocation(logId.intValue());

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
     * Gets the duration of a users logged in time.
     *
     * @param name the unique name of the user.
     * @return
     */
    private Duration getLoggedinTime(String name) {

        Query query = manager.createQuery("SELECT log.logTime FROM ICATLog log JOIN log.user user WHERE user.name= :name ORDER BY log.logTime desc");
        query.setParameter("name", name);
        query.setMaxResults(1);

        Date loginTime = (Date) query.getSingleResult();

        Duration loggedTime = Duration.between((convertToLocalDateTime(loginTime)), LocalDateTime.now());

        return loggedTime;

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
    private GeoLocation getLogLocation(int logId) {

        String locationQuery = "SELECT location from GeoLocation location JOIN location.logs log WHERE log.id='" + logId + "'";
        String ipQuery = "SELECT log.ipAddress FROM ICATLog log WHERE log.id='" + logId + "'";

        List<Object> location = manager.createQuery(locationQuery).getResultList();
        GeoLocation geoLocation;

        /*Location has not been set due to it being a functional account log. We do not store that to prevent
             * the geoLocation API blocking the dashboards ip.
         */
        if (location.isEmpty()) {
            List<Object> ipList = manager.createQuery(ipQuery).getResultList();
            geoLocation = GeoTool.getGeoLocation((String) ipList.get(0), manager, beanManager);

        } else {
            geoLocation = (GeoLocation) location.get(0);
        }

        return geoLocation;

    }

}
