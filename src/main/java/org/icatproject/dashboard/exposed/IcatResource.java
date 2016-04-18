/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.consumers.GeoTool;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.InternalException;
import static org.icatproject.dashboard.exposed.RestUtility.convertToLocalDateTime;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.IcatDataManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Stateless
@LocalBean
@Path("icat")
public class IcatResource {

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;
    
    @EJB
    IcatDataManager icatData;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    
    /**
     * Access the IcatDataManager to get the authenticators mnemonic;
     * @return a JSONArray containing the ICATs mnemonics
     * @throws InternalException issue accessing the IcatDataManager;
     */
    @GET
    @Path("authenticators")
    @Produces(MediaType.APPLICATION_JSON)
    public String getICATAuthenticators() throws InternalException {       

        return icatData.getAuthenticators();
    }

    /**
     * Retrieves the ICAT logs from that are stored in the ICATLog table.
     *
     * @param sessionID for authentication
     * @param queryConstraint the JPQL where query.
     * @param initialLimit the initial value of a limit by expression
     * @param maxLimit the max limit of a limit by expression.
     * @return a JSON array of ICAT Log JSON Objects.
     * @throws DashboardException Troubles accessing the database.
     */
    @GET
    @Path("logs")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIcatLogs(@QueryParam("sessionID") String sessionID,
            @QueryParam("queryConstraint") String queryConstraint,
            @QueryParam("initialLimit") int initialLimit,
            @QueryParam("maxLimit") int maxLimit) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

        String query = "SELECT log, user.fullName from ICATLog log JOIN log.user user ";

        //Check status of passed paramaters and build query.		
        if (!("".equals(queryConstraint))) {
            query += queryConstraint;
        }

        List<Object[]> logs = manager.createQuery(query).setFirstResult(initialLimit).setMaxResults(maxLimit).getResultList();

        JSONArray result = new JSONArray();

        for (Object[] log : logs) {
            JSONObject obj = new JSONObject();
            ICATLog tempLog = (ICATLog) log[0];
            obj.put("fullName", log[1]);
            obj.put("id", tempLog.getId());
            obj.put("entityId", tempLog.getEntityId());
            obj.put("entityType", tempLog.getEntityType());
            obj.put("ipAddress", tempLog.getIpAddress());
            obj.put("logTime", convertToLocalDateTime(tempLog.getLogTime()).toString());
            obj.put("op", tempLog.getOperation());
            obj.put("query", tempLog.getQuery());
            obj.put("duration", tempLog.getDuration());
            result.add(obj);

        }

        return result.toJSONString();
    }

    /**
     * Retrieves the geoLocation of an ICAT log. If the log was of a functional
     * account then the location will be retrieved with the geo tool as
     * functional account locations aren't stored.
     *
     * @param sessionID for authentication
     * @param logId the unique identifier of an ICAT log.
     * @return a JSON containing the city, longitude and latitude.
     * @throws DashboardException
     */
    @GET
    @Path("logs/location")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIcatLogLocation(@QueryParam("sessionID") String sessionID,
            @QueryParam("logId") int logId) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }

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
