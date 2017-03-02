/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import org.icatproject.dashboard.utility.RestUtility;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.DownloadEntity;
import org.icatproject.dashboard.entity.DownloadEntityAge;
import org.icatproject.dashboard.entity.Entity_;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.InternalException;
import org.icatproject.dashboard.exceptions.NotFoundException;
import org.icatproject.dashboard.exceptions.NotImplementedException;
import static org.icatproject.dashboard.exposed.PredicateCreater.createDownloadLocationPredicate;
import static org.icatproject.dashboard.exposed.PredicateCreater.createDownloadPredicate;
import static org.icatproject.dashboard.exposed.PredicateCreater.createDownloadPredicateEntity;
import static org.icatproject.dashboard.exposed.PredicateCreater.createJoinDatePredicate;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Comparator;

@Stateless
@LocalBean
@Path("/download")
public class DownloadRest {
    
    private static final Logger LOG = LoggerFactory.getLogger(DownloadRest.class);
    
    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    private final DecimalFormat df = new DecimalFormat("#.##");

    //Constants for download statuses.
    private final String preparing = "preparing";
    private final String inProgress = "inProgress";
    private final String finished = "finished";
    private final String failed = "failed";

    /**
     * Retrieves download data.
     *
     * @param sessionID for authentication.
     * @param queryConstraint any JPQL expression that can be appended to "SELECT download from Download download", e.g. "where download.id = 10". 
     * @param initialLimit the initial limit value. Similar to LIMIT in SQL with initial Limit being the first value.
     * @param maxLimit the end limit value.  Similar to LIMIT in SQL with max Limit being the scond value.
     * @return an array of downloads with each entry in the form of [{"downloadSize":6871,"method":"https","bandwidth":62463.6364,"geoId":2304836159,"downloadStart":"2016-06-13T09:55:54.860","name":"uows\/1424","fullName":"Mr ICAT","id":4912368247,"downloadEnd":"2016-06-13T09:55:54.970","status":"finished"}]
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
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloads(@QueryParam("sessionID") String sessionID,
            @QueryParam("queryConstraint") String queryConstraint,
            @QueryParam("initialLimit") int initialLimit,
            @QueryParam("maxLimit") int maxLimit) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        JSONArray ary = new JSONArray();

        String query = "SELECT download, user.name, user.fullName from Download download JOIN download.user user ";

        //Check status of passed paramaters and build query.
        if (!("".equals(queryConstraint))) {
            query += queryConstraint;
        }

        List<Object[]> downloads = manager.createQuery(query).setFirstResult(initialLimit).setMaxResults(maxLimit).getResultList();

        for (Object[] singleDownload : downloads) {
            JSONObject obj = new JSONObject();
            Download d = (Download) singleDownload[0];
            obj.put("downloadSize", d.getDownloadSize());
            obj.put("id", d.getId());
            obj.put("method", d.getMethod());
            obj.put("status", d.getStatus());
            obj.put("fullName", singleDownload[2]);
            obj.put("name", singleDownload[1]);
            obj.put("geoId",d.getLocation().getId());

            //Handle preparing downloads as they wont have a start date
            if (!("preparing".equals(d.getStatus()))) {
                obj.put("downloadStart", convertToLocalDateTime(d.getDownloadStart()).toString());
            }

            //To deal with unfinished downloads as it wont have bandiwdth or end date.
            if ("finished".equals(d.getStatus())) {
                obj.put("downloadEnd", convertToLocalDateTime(d.getDownloadEnd()).toString());
                obj.put("bandwidth", d.getBandwidth());
            } else {
                //Bandiwdth is unknown so should return 0.
                obj.put("bandwidth", 0);
            }

            ary.add(obj);

        }

        return ary.toJSONString();
    }
    
    /**
     * Retrieves the number of downloads per file extension.
     *
     * @param sessionID for authentication
     * @param startDate Start time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate End time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param method the method of download
     * @return A JSON array of JSON objects in the format of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":0},{"date":"2016-06-09","number":31},{"date":"2016-06-10","number":1},{"date":"2016-06-11","number":0},{"date":"2016-06-12","number":0},{"date":"2016-06-13","number":8},{"date":"2016-06-14","number":0},{"date":"2016-06-15","number":0},{"date":"2016-06-16","number":0}]
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
    @Path("files/extension")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFileExtensionDownloadFrequency(@QueryParam("sessionID") String sessionID,
                                           @QueryParam("startDate") String startDate,
                                           @QueryParam("endDate") String endDate,
                                           @QueryParam("method") String method) throws DashboardException {
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));
        
        JSONArray ary = new JSONArray();
        
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<Entity_> entity = query.from(Entity_.class);
        
        query.select(entity.get("entityName"));
        Predicate finalPredicate = createDownloadPredicateEntity(cb, start, end, entity, method);
        query.where(finalPredicate);
        TypedQuery<Object> typedQuery = manager.createQuery(query);
        List<Object> entities = typedQuery.getResultList();
        Map<String, Integer> pairList = new HashMap<>();

        for (Object singleDownload : entities) {
            String entityName = (String) singleDownload;
            String extension = null;
            int i = entityName.lastIndexOf('.');
            if (i > 0) {
                extension = entityName.substring(i+1).toUpperCase();
            }
            else {
                continue;
            }
            
            boolean inHashmap = false;
            
            for (Map.Entry<String, Integer> entry : pairList.entrySet()) {
                if (entry.getKey().equals(extension)) {
                    entry.setValue(entry.getValue() + 1);
                    inHashmap = true;
                    break;
                }
            }
            
            if (!inHashmap) {
                pairList.put(extension, 1);
            }
        }
        
        for (Map.Entry<String, Integer> entry : pairList.entrySet()) {
            JSONObject temp = new JSONObject();
            temp.put("entityName", entry.getKey());
            temp.put("count", entry.getValue());
            ary.add(temp);
        }

        return ary.toJSONString();
    }
    
    /**
     * Retrieves the number of downloads per format of download.
     *
     * @param sessionID for authentication
     * @param startDate Start time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate End time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param method the method of download
     * @return A JSON array of JSON objects in the format of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":0},{"date":"2016-06-09","number":31},{"date":"2016-06-10","number":1},{"date":"2016-06-11","number":0},{"date":"2016-06-12","number":0},{"date":"2016-06-13","number":8},{"date":"2016-06-14","number":0},{"date":"2016-06-15","number":0},{"date":"2016-06-16","number":0}]
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
    @Path("files/format")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFormatDownloadFrequency(@QueryParam("sessionID") String sessionID,
                                           @QueryParam("startDate") String startDate,
                                           @QueryParam("endDate") String endDate,
                                           @QueryParam("method") String method) throws DashboardException {
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));
        
        JSONArray ary = new JSONArray();
        
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<Entity_> entity = query.from(Entity_.class);
        
        query.select(entity.get("type"));
        Predicate finalPredicate = createDownloadPredicateEntity(cb, start, end, entity, method);
        query.where(finalPredicate);
        TypedQuery<Object> typedQuery = manager.createQuery(query);
        List<Object> entities = typedQuery.getResultList();
        Map<String, Integer> pairList = new HashMap<>();
        
        for (Object singleDownload : entities) {
            String formatType = (String) singleDownload;
            
            boolean inHashmap = false;
            
            for (Map.Entry<String, Integer> entry : pairList.entrySet()) {
                if (entry.getKey().equals(formatType)) {
                    entry.setValue(entry.getValue() + 1);
                    inHashmap = true;
                    break;
                }
            }
            
            if (!inHashmap) {
                pairList.put(formatType, 1);
            }
        }

        for (Map.Entry<String, Integer> entry : pairList.entrySet()) {
            JSONObject temp = new JSONObject();
            temp.put("type", entry.getKey());
            temp.put("count", entry.getValue());
            ary.add(temp);
        }

        return ary.toJSONString();
    }
    
    /**
     * Retrieves the geoLocation of an Download
     *
     * @param sessionID for authentication
     * @param downloadId the unique identifier of an download.
     * @return a JSON array in the format [{"number":1,"city":"Abingdon","countryCode":"United Kingdom","latitude":51.6711,"isp":"Science and Technology Facilites Council","longitude":-1.2828}]
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
    @Path("location/{downloadId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIcatLogLocation(@QueryParam("sessionID") String sessionID,
                                     @PathParam("downloadId") Long downloadId) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        GeoLocation geoLocation = getDownloadLocation(downloadId);

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
     * Returns all the information on download entities.
     *
     * @param sessionID for authentication.
     * @param queryConstraint any JPQL expression that can be appended to "SELECT entity from DownloadEntity entity", e.g. "where entity.type = 'Datafile'". 
     * @param initialLimit the initial limit value. Similar to LIMIT in SQL with initial Limit being the first value.
     * @param maxLimit the end limit value.  Similar to LIMIT in SQL with max Limit being the scond value.
     * @return a JSON array of the format [{"creationTime":"2016-05-11T00:21:16.256","icatId":80768335,"entityName":"WISH00034921.log","type":"datafile","entitySize":93903}]
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
    @Path("entity")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloadEntities(@QueryParam("sessionID") String sessionID,
            @QueryParam("queryConstraint") String queryConstraint,
            @QueryParam("initialLimit") int initialLimit,
            @QueryParam("maxLimit") int maxLimit) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }     

        String query = "SELECT entity from Entity_ entity JOIN entity.downloadEntities de JOIN de.download download ";

        //Check status of passed paramaters and build query.
        if (!("".equals(queryConstraint))) {
            query += queryConstraint;
        }

        Object[] entities = manager.createQuery(query).getResultList().toArray();

        JSONArray result = new JSONArray();

        for (Object e : entities) {
            JSONObject t = new JSONObject();
            Entity_ entityResult = (Entity_) e;
            t.put("entityName", entityResult.getEntityName());
            t.put("entitySize", entityResult.getEntitySize());
            t.put("icatId", entityResult.getIcatId());
            t.put("type", entityResult.getType());
            t.put("creationTime", convertToLocalDateTime(entityResult.getICATcreationTime()).toString());
            result.add(t);
        }

        return result.toJSONString();
    }
    
    /**
     * *
     * Retrieves the age of files in days of every download within the set
     * parameters.
     *
     * @param sessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName Unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method type of download method. e.g. https, 
     * @return a JSONArray of JSONObjects in the format [{"number":1,"age":1},{"number":4,"age":34},{"number":1,"age":54},{"number":3,"age":91},{"number":32,"age":344},{"number":9,"age":33},{"number":28,"age":345}]
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
    @Path("entity/age")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEntityAge(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName,
            @QueryParam("method") String method) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<DownloadEntityAge> downloadEntityAge = query.from(DownloadEntityAge.class);

        //Join between downloadEntityAge, downloads and users.
        Join<DownloadEntityAge, Download> downloadJoin = downloadEntityAge.join("download");
        Join<Download, ICATUser> downloadUserJoin = downloadJoin.join("user");

        query.multiselect(cb.sum(downloadEntityAge.<Long>get("amount")), downloadEntityAge.get("age"));

        Predicate finalPredicate = createJoinDatePredicate(cb,"downloadStart","downloadEnd",start,end,downloadJoin);

        if (!("undefined".equals(method)) && !("".equals(method))) {
            Predicate methodPredicate = cb.equal(downloadJoin.get("method"), method);
            finalPredicate = cb.and(finalPredicate, methodPredicate);
        }

        if (!("undefined".equals(userName)) && !(("").equals(userName))) {
            Predicate userPredicate = cb.equal(downloadUserJoin.get("name"), userName);
            finalPredicate = cb.and(finalPredicate, userPredicate);
        }

        query.groupBy(downloadEntityAge.get("age"));

        query.where(finalPredicate);

        List<Object[]> downloads = manager.createQuery(query).getResultList();

        JSONArray ary = new JSONArray();

        for (Object[] download : downloads) {
            JSONObject obj = new JSONObject();
            obj.put("number", download[0]);
            obj.put("age", download[1]);
            ary.add(obj);
        }

        return ary.toJSONString();

    }

    /**
     * Retrieves the number of downloads that occurred over the provided
     * period.
     *
     * @param sessionID for authentication
     * @param startDate Start time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate End time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName Unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method the method of download
     * @return A JSON array of JSON objects in the format of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":0},{"date":"2016-06-09","number":31},{"date":"2016-06-10","number":1},{"date":"2016-06-11","number":0},{"date":"2016-06-12","number":0},{"date":"2016-06-13","number":8},{"date":"2016-06-14","number":0},{"date":"2016-06-15","number":0},{"date":"2016-06-16","number":0}]
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
    @Path("frequency")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloadFrequency(@QueryParam("sessionID") String sessionID,
                                        @QueryParam("startDate") String startDate,
                                        @QueryParam("endDate") String endDate,
                                        @QueryParam("userName") String userName,
                                        @QueryParam("method") String method) throws DashboardException {
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate();

        TreeMap<LocalDate, Long> downloadDates = RestUtility.createPrePopulatedLongMap(startRange, endRange);

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many there are.
        query.multiselect(download.get("downloadStart"), download.get("downloadEnd"));

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, userName, method);

        query.where(finalPredicate);
        
        List<Object[]> downloads = manager.createQuery(query).getResultList();

        for (Object[] singleDownload : downloads) {

            LocalDate downloadBeginning = convertToLocalDate((Date) singleDownload[0]);
            LocalDate downloadEnd;
            //To deal with downloads still currently going. Just set it to the current date
            if (singleDownload[1] == null) {             
                downloadEnd = LocalDate.now();
            } else {
                downloadEnd = convertToLocalDate((Date) singleDownload[1]);
            }

            //Bring the download date up to the requested start date.
            while (downloadBeginning.isBefore(startRange)) {
                downloadBeginning = downloadBeginning.plusDays(1);

            }

            while ((!downloadBeginning.isAfter(endRange)) && (!downloadBeginning.isAfter(downloadEnd))) {
                Long currentTotal = downloadDates.get(downloadBeginning);
                downloadDates.put(downloadBeginning, currentTotal += 1);
                downloadBeginning = downloadBeginning.plusDays(1);
            }
        }

        return RestUtility.convertMapToJSON(downloadDates).toJSONString();

    }

    /**
     * Retrieves the amount of downloads per person over the requested period.
     *
     * @param sessionID for authentication
     * @param startDate Start time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate End time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param method the method of download e.g. https, globus, scarf
     * @return A JSON array of JSON Objects in the format of [{"name":"uows\/1024006","count":6,"fullName":"Mr ICAT"},{"name":"uows\/105225","count":3,"fullName":"Mr Bob"}]
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
    @Path("frequency/users")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserDownloadFrequency(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("method") String method) throws DashboardException {
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many there are.
        query.multiselect(cb.count(download), userJoin.get("name"));
        
        // Get the number of users that should be shown on the pie chart
        int numberOfUsers = properties.getNumberOfDownloads();

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, "", method);

        query.where(finalPredicate);
        query.groupBy(userJoin.get("name"));
        List<Object[]> users = manager.createQuery(query).getResultList();
        
        // This will sort the list of arrays by the number of downloads per user.
        Collections.sort(users, new Comparator<Object[]>() {
            public int compare(Object[] someUser, Object[] otherUser) {
                return Long.compare((long)otherUser[0], (long)someUser[0]);
            }
        });
        
        JSONArray result = new JSONArray();
        
        // Only want the top X and sort rest into "others"
        if (users.size() > numberOfUsers) {
            List<Object[]> others = users.subList(numberOfUsers, users.size());
            // Calculate the total size of the others category
            Long othersCount = (long) 0;
            
            for (Object[] user : others) {
                othersCount += (Long) user[0];
            }
            
            // Create the JSON other object
            JSONObject otherObject = new JSONObject();
            otherObject.put("fullName", "Other");
            otherObject.put("name", "Other");
            otherObject.put("count", othersCount);
            
            // Add the object to the result
            result.add(otherObject);
            
            users = users.subList(0, numberOfUsers);
        }

        for (Object[] user : users) {
            JSONObject temp = new JSONObject();

            String name = (String) user[1];
            Long count = (Long) user[0];
            // Need to get the fullName of the user.
            temp.put("fullName", RestUtility.getFullName(name, manager));
            temp.put("name", name);
            temp.put("count", count);

            result.add(temp);
        }

        return result.toJSONString();

    }
    
    /**
     * Retrieves the volume of downloads per user.
     *
     * @param sessionID for authentication
     * @param startDate start time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param method the method of download e.g. https, globus, scarf
     * @return A JSON array of JSON Objects in the form of [{"volume":344349230,"name":"uows\/10506","fullName":"Mr Bob Doe"}].
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
    @Path("volume/user")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserDownloadVolume(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("method") String method) throws DashboardException {
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many their are.
        query.multiselect(cb.sum(download.<Long>get("downloadSize")), userJoin.get("name"));

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, "", method);

        query.where(finalPredicate);

        query.groupBy(userJoin.get("name"));

        List<Object[]> users = manager.createQuery(query).getResultList();
        
        // This will sort the list of arrays by the number of downloads per user.
        Collections.sort(users, new Comparator<Object[]>() {
            public int compare(Object[] someUser, Object[] otherUser) {
                return Long.compare((long)otherUser[0], (long)someUser[0]);
            }
        });
        
        // Only want the top 10.
        if (users.size() > 10) {
            users = users.subList(0, 10);
        }

        JSONArray result = new JSONArray();

        for (Object[] user : users) {
            JSONObject temp = new JSONObject();

            String name = (String) user[1];
            Long volume = (Long) user[0];
            //Need to get the fullName of the user.
            temp.put("fullName", RestUtility.getFullName(name, manager));
            temp.put("name", name);
            temp.put("volume", volume);

            result.add(temp);

        }

        return result.toJSONString();

    }

    /**
     * Retrieves the number of failed and successful downloads.
     *
     * @param sessionID For authentication
     * @param startDate Start time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate End time in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName Unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method the method of download e.g. https, globus, scarf
     * @return A JSON object of the format [{"number":40,"status":"finished"}]
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
    @Path("status/number")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloadStatusNumber(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName,
            @QueryParam("method") String method) throws BadRequestException, NotImplementedException, AuthenticationException, InternalException, NotFoundException  {
        
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many their are.
        query.multiselect(download.get("status"), cb.count(download));

        Predicate finalPredicate = cb.and(createDownloadPredicate(cb, start, end, download, userJoin, userName, method));

        query.where(finalPredicate);

        query.groupBy(download.get("status"));

        List<Object[]> downloadStatusCount = manager.createQuery(query).getResultList();

        JSONArray result = new JSONArray();
        for (Object[] downloadStatus : downloadStatusCount) {
            JSONObject obj = new JSONObject();
            obj.put("status", downloadStatus[0]);
            obj.put("number", downloadStatus[1]);
            result.add(obj);

        }

        return result.toString();

    }

    /**
     * Retrieves the number of downloads per download method e.g. Globus https etc with the amount
     * of downloads that used those methods.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end points for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName Unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @return a JSON Array in the format of [{"number":1,"method":"globus"},{"number":39,"method":"https"}]
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
    @Path("method/number")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMethodNumber(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName) throws DashboardException {
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many their are.
        query.multiselect(download.get("method"), cb.count(download.get("method")));

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, userName, "");

        query.where(finalPredicate);

        //Finally group by the method
        query.groupBy(download.get("method"));

        List<Object[]> methods = manager.createQuery(query).getResultList();

        JSONArray ary = new JSONArray();

        for (Object[] result : methods) {
            JSONObject obj = new JSONObject();
            String method = result[0].toString();
            long number = (long) result[1];
            obj.put("number", number);
            obj.put("method", method);
            ary.add(obj);
        }

        return ary.toJSONString();

    }

    /**
     * Retrieves the volume of downloads per download method.
     *
     * @param sessionID for authentication.
     * @param startDate start point for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end points for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @return a JSON Array in the format of [{"volume":16232,"method":"globus"},{"volume":385674761,"method":"https"}]
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
    @Path("method/volume")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMethodVolume(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName) throws DashboardException {
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many there are.
        query.multiselect(download.get("method"), cb.sum(download.<Long>get("downloadSize")));

        //Create a where clause that deals with the provided query params.
        Predicate generalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, userName, "");

        //Make sure only finished downloads are collected as the volume downloaded is unknown.
        Predicate finishedPrecicate = cb.equal(download.get("status"), finished);

        query.where(cb.and(generalPredicate, finishedPrecicate));

        //Finally group by the method
        query.groupBy(download.get("method"));

        List<Object[]> methods = manager.createQuery(query).getResultList();

        JSONArray ary = new JSONArray();

        for (Object[] result : methods) {
            JSONObject obj = new JSONObject();
            String method = result[0].toString();
            long number = (long) result[1];
            obj.put("volume", number);
            obj.put("method", method);
            ary.add(obj);
        }

        return ary.toJSONString();

    }

    /**
     * *
     * Retrieves all the types of download methods used by the TopCat
     *
     * @param sessionID for authentication
     * 
     * @return JSONArray of JSONObjects of the format [{"name":"globus"},{"name":"https"}]
     * 
     * @throws BadRequestException     
     * @throws NotImplementedException
     * @throws AuthenticationException
     * @throws InternalException
     * @throws NotFoundException    
     * 
     * 
     * 
     * @statuscode 200 To indicate success
     */
    @GET
    @Path("method/types")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMethods(@QueryParam("sessionID") String sessionID) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        List<String> methodTypes;

        methodTypes = manager.createNamedQuery("Download.method.types").getResultList();

        JSONArray ary = new JSONArray();

        for (int i = 0; i < methodTypes.size(); i++) {
            JSONObject obj = new JSONObject();
            String method = methodTypes.get(i);
            obj.put("name", method);
            ary.add(obj);
        }

        return ary.toJSONString();
    }

    /**
     * *
     * Retrieves the min, max average bandwidth of downloads within the provided dates grouped by
     * the ISP.
     *
     * @param sessionID for authentication.
     * @param startDate start point for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end points for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method the method of download e.g. https, globus, scarf.
     * 
     * @return JSON object in a JSONArray of the format [{"average":"10936668.55","min":"554.05","max":"45172744.47","isp":"Science and Technology Facilites Council"}] 
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
    @Path("bandwidth/isp")
    @Produces(MediaType.APPLICATION_JSON)
    public String getISPBandwidth(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName,
            @QueryParam("method") String method) throws DashboardException {
        
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);

        //Join between download and download location.
        Join<Download, GeoLocation> downloadJoin = download.join("location");

        Join<Download, ICATUser> userJoin = download.join("user");

        query.multiselect(cb.avg(download.<Long>get("bandwidth")), cb.min(download.<Long>get("bandwidth")), cb.max(download.<Long>get("bandwidth")), downloadJoin.get("isp"));        

        Predicate completeDownload = cb.equal(download.get("status"), finished);       
        
        Predicate finalPredicate = cb.and(completeDownload, createDownloadPredicate(cb, start, end, download, userJoin, userName, method));
        
        query.groupBy(downloadJoin.get("isp"));

        query.where(finalPredicate);

        List<Object[]> downloads = manager.createQuery(query).getResultList();

        JSONArray container = new JSONArray();

        for (Object[] singleDownload : downloads) {

            JSONObject downloadData = new JSONObject();
            downloadData.put("average", df.format(Double.parseDouble(singleDownload[0].toString())));
            downloadData.put("min", df.format(Double.parseDouble(singleDownload[1].toString())));
            downloadData.put("max", df.format(Double.parseDouble(singleDownload[2].toString())));
            downloadData.put("isp", singleDownload[3]);

            container.add(downloadData);

        }

        return container.toJSONString();
    }

    /**
     * Retrieves the volume of data per day downloaded.
     *
     * @param sessionID for authentication.
     * @param startDate start point for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end points for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method the method of download e.g. https, globus, scarf.
     * @return A JSONArray of JSONObjects in the format of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":0},{"date":"2016-06-09","number":41293067},{"date":"2016-06-10","number":287},{"date":"2016-06-11","number":0},{"date":"2016-06-12","number":0},{"date":"2016-06-13","number":344397639},{"date":"2016-06-14","number":0}]
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
    @Path("volume")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSize(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName,
            @QueryParam("method") String method) throws DashboardException {
        
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        LocalDate startRange = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endRange = Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate();

        DownloadSizeProcessor downloadHelper = new DownloadSizeProcessor(startRange, endRange);

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many their are.
        query.multiselect(download.get("downloadStart"), download.get("downloadEnd"), download.get("downloadSize"));

        //Create a where clause that deals with the provided query params.
        Predicate generalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, userName, method);

        //Make sure only finished downloads are collected as the volume downloaded is unknown.
        Predicate finishedPrecicate = cb.equal(download.get("status"), finished);

        query.where(cb.and(generalPredicate, finishedPrecicate));
        
        List<Object[]> downloads = manager.createQuery(query).getResultList();

        TreeMap<LocalDate, Long> downloadDates = downloadHelper.calculateDataDownloaded(downloads);

        return RestUtility.convertMapToJSON(downloadDates).toJSONString();
    }

    

    /**
     * *
     * Retrieves the number of downloads in each country.
     *
     * @param sessionID for authentication.
     * @param startDate Start point for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end points for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method the method of download e.g. https, globus, scarf.
     * @return a JSON String in the format {countryCode:GB, Number:10}
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
    @Path("location/global")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloadGlobalLocations(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName,
            @QueryParam("method") String method) throws DashboardException {


        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<GeoLocation> geoLocation = query.from(GeoLocation.class);

        //Join between downloads and location.
        Join<Download, GeoLocation> downloadLocationJoin = geoLocation.join("downloads");

        //Get methods and count how many their are.
        query.multiselect(geoLocation.get("countryCode"), cb.count(geoLocation.get("countryCode")));

        Predicate finalPredicate = createDownloadLocationPredicate(cb, start, end, downloadLocationJoin, userName, method);

        query.where(finalPredicate);

        //Finally group by the method
        query.groupBy(geoLocation.get("countryCode"));

        List<Object[]> downloadGlobalLocations = manager.createQuery(query).getResultList();

        JSONArray resultArray = new JSONArray();

        for (Object[] download : downloadGlobalLocations) {
            JSONObject obj = new JSONObject();
            obj.put("countryCode", download[0]);
            obj.put("number", download[1]);

            resultArray.add(obj);

        }

        return resultArray.toJSONString();
    }

    /**
     * *
     * Retrieves the number of downloads in each set of longitude and latitude pairs.
     *
     * @param sessionID for authentication.
     * @param startDate Start point for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate end points for downloads in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param userName unique name of the user. Corresponds to name in the ICAT user table. In the form of authenticator/name.
     * @param method the method of download e.g. https, globus, scarf.
     * @return a JSON String in the format {city:Appelton, Number:10, Longitude:20.20, Latitude:-1.34}
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
    public String getDownloadLocations(@QueryParam("sessionID") String sessionID,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("userName") String userName,
            @QueryParam("method") String method) throws DashboardException {
        
        if (sessionID == null) {
            throw new BadRequestException("A SessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<GeoLocation> downloadLocation = query.from(GeoLocation.class);

        Join<Download, GeoLocation> downloadLocationJoin = downloadLocation.join("downloads");

        Predicate finalPredicate = createDownloadLocationPredicate(cb, start, end, downloadLocationJoin, userName, method);

        //Get methods and count how many their are.
        query.multiselect(downloadLocation, cb.count(downloadLocationJoin));

        //Predicate finalPredicate = createDownloadLocationPredicate(cb,start,end,geoLocation,userName, method);     
        query.where(finalPredicate);

        //Finally group by the method
        query.groupBy(downloadLocation);

        List<Object[]> downloadLocalLocations = manager.createQuery(query).getResultList();

        JSONArray resultArray = new JSONArray();

        for (Object[] download : downloadLocalLocations) {
            JSONObject obj = new JSONObject();
            obj.put("number", download[1]);
            obj.put("city", ((GeoLocation) download[0]).getCity());
            obj.put("longitude", ((GeoLocation) download[0]).getLongitude());
            obj.put("latitude", ((GeoLocation) download[0]).getLatitude());
            resultArray.add(obj);

        }

        return resultArray.toJSONString();
    }

    /**
     * Retrieves the geolocation of an Download.
     *
     * @param downloadId the id of the log.
     * @return A geoLocation object of where the download took place.
     */
    private GeoLocation getDownloadLocation(Long downloadId) {

        String locationQuery = "SELECT location from GeoLocation location JOIN location.downloads download WHERE download.id='" + downloadId + "'";       

        List<Object> geoLocation = manager.createQuery(locationQuery).getResultList();        

        return (GeoLocation)geoLocation.get(0);

    }
    
    @GET
    @Path("period")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloadPeriod() {
        /*
        JSONObject object = new JSONObject();
        object.put("period", properties.getDownloadDays());
        return object.toString();
        */
        return Integer.toString(properties.getDownloadDays());
    }

    
}
