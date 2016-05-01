/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.DownloadEntityAge;
import org.icatproject.dashboard.entity.Entity_;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import static org.icatproject.dashboard.exposed.RestUtility.convertToLocalDateTime;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Stateless
@LocalBean
@Path("download")
public class DownloadResource {

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
     * Returns all the information on downloads.
     *
     * @param sessionID SessionID for authentication.
     * @param queryConstraint the where query
     * @param initialLimit the initial limit value.
     * @param maxLimit the end limit value.
     * @return All the information on downloads.
     * @throws BadRequestException Incorrect date formats or a invalid
     * sessionID.
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

        JSONArray ary = new JSONArray();

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
     * Gets the age of files in days of every download within the set
     * parameters.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @param method type of download method.
     * @return a JSONArray of JSONObjects in the format {number:10,age:200}
     * @throws DashboardException Issues with accessing the dashboard database.
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

        Predicate startGreater = cb.greaterThan(downloadJoin.<Date>get("downloadStart"), start);
        Predicate endLess = cb.lessThan(downloadJoin.<Date>get("downloadEnd"), end);
        Predicate betweenStart = cb.between(downloadJoin.<Date>get("downloadStart"), start, end);
        Predicate betweenEnd = cb.between(downloadJoin.<Date>get("downloadEnd"), start, end);

        Predicate combineBetween = cb.or(betweenStart, betweenEnd);
        Predicate combineGL = cb.and(startGreater, endLess);
        Predicate finalPredicate = cb.or(combineBetween, combineGL);

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
     * Calculates the number of downloads that occurred over the provided
     * period.
     *
     * @param sessionID For authentication
     * @param startDate Start time in a Unix timestamp.
     * @param endDate End time in a Unix timestamp.
     * @param userName Unique name of the user. Corresponds to name in the ICAT
     * user table.
     * @param method the method of download
     * @return A JSON array of JSON objects with each day between the provided
     * times
     * @throws DashboardException
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

        TreeMap<LocalDate, Long> downloadDates = RestUtility.createPrePopulatedMap(startRange, endRange);

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Download> download = query.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        //Get methods and count how many their are.
        query.multiselect(download.get("downloadStart"), download.get("downloadEnd"));

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, userName, method);

        query.where(finalPredicate);

        List<Object[]> downloads = manager.createQuery(query).getResultList();

        for (Object[] singleDownload : downloads) {

            LocalDate downloadBeginning = RestUtility.convertToLocalDate((Date) singleDownload[0]);
            LocalDate downloadEnd;
            //To deal with downloads still currently going. Just set it to the current date
            if (singleDownload[1] == null) {             
                downloadEnd = LocalDate.now();
            } else {
                downloadEnd = RestUtility.convertToLocalDate((Date) singleDownload[1]);
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
     * Calculates the amount of downloads per person.
     *
     * @param sessionID For authentication
     * @param startDate Start time in a Unix timestamp.
     * @param endDate End time in a Unix timestamp.
     * @param method the method of download
     * @return A JSON array of JSON Objects containing the name of the user and
     * the number of downloads.
     * @throws DashboardException
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

        //Get methods and count how many their are.
        query.multiselect(cb.count(download), userJoin.get("name"));

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, "", method);

        query.where(finalPredicate);

        query.groupBy(userJoin.get("name"));

        List<Object[]> users = manager.createQuery(query).getResultList();

        JSONArray result = new JSONArray();

        for (Object[] user : users) {
            JSONObject temp = new JSONObject();

            String name = (String) user[1];
            Long count = (Long) user[0];
            //Need to get the fullName of the user.
            temp.put("fullName", RestUtility.getFullName(name, manager));
            temp.put("name", name);
            temp.put("count", count);

            result.add(temp);

        }

        return result.toJSONString();

    }

    /**
     * Calculates the number of failed and successful downloads.
     *
     * @param sessionID For authentication
     * @param startDate Start time in a Unix timestamp.
     * @param endDate End time in a Unix timestamp.
     * @param userName Unique name of the user. Corresponds to name in the ICAT
     * user table.
     * @param method the method of download
     * @return A JSON object of how many have failed and how many have been
     * successful.
     * @throws DashboardException
     */
    @GET
    @Path("status/number")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDownloadStatusNumber(@QueryParam("sessionID") String sessionID,
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
     * Gets the routes used by downloads e.g. Globus https etc with the amount
     * of downloads that used those methods.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @return The type of route and the number of times used over the set
     * period.
     * @throws BadRequestException Incorrect date formats or a invalid
     * sessionID.
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
     * Gets the routes used by downloads e.g. Globus.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @return The type of route and the number of times used over the set
     * period.
     * @throws BadRequestException Incorrect date formats or a invalid
     * sessionID.
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

        //Get methods and count how many their are.
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
     * Calculates the amount of downloads per person.
     *
     * @param sessionID For authentication
     * @param startDate Start time in a Unix timestamp.
     * @param endDate End time in a Unix timestamp.
     * @param method the method of download
     * @return A JSON array of JSON Objects containing the name of the user and
     * the number of downloads.
     * @throws DashboardException
     */
    @GET
    @Path("method/volume/user")
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
     * *
     * Gets all the types of download methods used in the ICAT family.
     *
     * @param sessionID To authenticate the user.
     * @return a JSON containing all the different methods of downloads.
     * @throws AuthenticationException Invalid sessionID.
     * @throws BadRequestException No session ID provided.
     */
    @GET
    @Path("method/types")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMethods(@QueryParam("sessionID") String sessionID) throws AuthenticationException, BadRequestException {

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
     * Returns the bandwidth of downloads within the provided dates grouped by
     * the ISP.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @param method type of download method.
     * @return JSON object includes the min, max and average of the ISP
     * bandwidth during that period.
     * @throws DashboardException
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

        Join<Download, ICATUser> downloadUserJoin = download.join("user");

        query.multiselect(cb.avg(download.<Long>get("bandwidth")), cb.min(download.<Long>get("bandwidth")), cb.max(download.<Long>get("bandwidth")), downloadJoin.get("isp"));

        Predicate startGreater = cb.greaterThan(download.<Date>get("downloadStart"), start);
        Predicate endLess = cb.lessThan(download.<Date>get("downloadEnd"), end);
        Predicate betweenStart = cb.between(download.<Date>get("downloadStart"), start, end);
        Predicate betweenEnd = cb.between(download.<Date>get("downloadEnd"), start, end);

        Predicate completeDownload = cb.equal(download.get("status"), finished);

        Predicate combineBetween = cb.or(betweenStart, betweenEnd);
        Predicate combineGL = cb.and(startGreater, endLess);
        Predicate finalPredicate = cb.and(completeDownload, cb.or(combineBetween, combineGL));

        if (!("undefined".equals(method)) && !("".equals(method))) {
            Predicate methodPredicate = cb.equal(download.get("method"), method);
            finalPredicate = cb.and(finalPredicate, methodPredicate);
        }

        if (!("undefined".equals(userName)) && !(("").equals(userName))) {
            Predicate userPredicate = cb.equal(downloadUserJoin.get("name"), userName);
            finalPredicate = cb.and(finalPredicate, userPredicate);
        }

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
     * Calculates the number of data that was downloaded over a set period of
     * time.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @param method type of download method.
     * @return A JSONArray of each date between the start and end and how much
     * data was downloaded.
     * @throws DashboardException Issues with accessing the data from the
     * database.
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
     * Gets the number of downloads in each country.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @param method type of download method.
     * @return a JSON String in the format {countryCode:GB, Number:10}
     * @throws DashboardException issue with accessing the database
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
     * Gets the number of downloads in each set of longitude and latitude pairs.
     *
     * @param sessionID SessionID for authentication.
     * @param startDate Start point for downloads.
     * @param endDate end points for downloads.
     * @param userName name of the user to check against.
     * @param method type of download method.
     * @return a JSON String in the format {city:Appelton, Number:10,
     * Longitude:20.20, Latitude:-1.34}
     * @throws DashboardException issue with accessing the database
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
     * *
     * Creates a predicate that applies a restriction to gather all downloads
     * between the start and end date and any during those period.
     *
     * @param cb CriteriaBuilder to build the Predicate.
     * @param start Start time of the predicate statement.
     * @param end End time of the predicate statement.
     * @param userName The name of a ICATuser to add to the predicate.
     * @param method The name of a method to add to the predicate.
     * @return a predicate object that contains restrictions to gather all
     * downloads during the start and end date.
     */
    private Predicate createDownloadPredicate(CriteriaBuilder cb, Date start, Date end, Root<Download> download, Join<Download, ICATUser> userJoin, String userName, String method) {

        Predicate startGreater = cb.greaterThan(download.<Date>get("downloadStart"), start);
        Predicate endLess = cb.lessThan(download.<Date>get("downloadEnd"), end);
        Predicate betweenStart = cb.between(download.<Date>get("downloadStart"), start, end);
        Predicate betweenEnd = cb.between(download.<Date>get("downloadEnd"), start, end);

        Predicate combineBetween = cb.or(betweenStart, betweenEnd);
        Predicate combineGL = cb.and(startGreater, endLess);
        Predicate finalPredicate = cb.or(combineBetween, combineGL);

        if (!("undefined".equals(method)) && !("".equals(method))) {
            Predicate methodPredicate = cb.equal(download.get("method"), method);
            finalPredicate = cb.and(finalPredicate, methodPredicate);
        }

        if (!("undefined".equals(userName)) && !(("").equals(userName))) {
            Predicate userPredicate = cb.equal(userJoin.get("name"), userName);
            finalPredicate = cb.and(finalPredicate, userPredicate);
        }

        return finalPredicate;

    }

    /**
     * *
     * Creates a predicate that applies a restriction to gather all
     * downloadLocations between the start and end date and any during those
     * period.
     *
     * @param cb CriteriaBuilder to build the Predicate.
     * @param start Start time of the predicate statement.
     * @param end End time of the predicate statement.
     * @param userName The name of a ICATuser to add to the predicate.
     * @param method The name of a method to add to the predicate.
     * @return a predicate object that contains restrictions to gather all
     * downloadLocations during the start and end date.
     */
    private Predicate createDownloadLocationPredicate(CriteriaBuilder cb, Date start, Date end, Join<Download, GeoLocation> downloadLocationJoin, String userName, String method) {

        Predicate startGreater = cb.greaterThan(downloadLocationJoin.<Date>get("downloadStart"), start);
        Predicate endLess = cb.lessThan(downloadLocationJoin.<Date>get("downloadEnd"), end);
        Predicate betweenStart = cb.between(downloadLocationJoin.<Date>get("downloadStart"), start, end);
        Predicate betweenEnd = cb.between(downloadLocationJoin.<Date>get("downloadEnd"), start, end);

        Predicate combineBetween = cb.or(betweenStart, betweenEnd);
        Predicate combineGL = cb.and(startGreater, endLess);
        Predicate finalPredicate = cb.or(combineBetween, combineGL);

        if (!("undefined".equals(method)) && !("".equals(method))) {
            Predicate methodPredicate = cb.equal(downloadLocationJoin.get("method"), method);
            finalPredicate = cb.and(finalPredicate, methodPredicate);
        }

        if (!("undefined".equals(userName)) && !(("").equals(userName))) {
            Join<ICATUser, Download> downloadUserJoin = downloadLocationJoin.join("user");
            Predicate userPredicate = cb.equal(downloadUserJoin.get("name"), userName);
            finalPredicate = cb.and(finalPredicate, userPredicate);
        }

        return finalPredicate;
    }
}
