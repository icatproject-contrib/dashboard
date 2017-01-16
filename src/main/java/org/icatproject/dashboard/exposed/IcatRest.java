/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.consumers.GeoTool;
import org.icatproject.dashboard.entity.EntityCount;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.entity.InstrumentMetaData;
import org.icatproject.dashboard.entity.InvestigationMetaData;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.GetLocationException;
import org.icatproject.dashboard.exceptions.InternalException;
import static org.icatproject.dashboard.exposed.PredicateCreater.getDatePredicate;
import static org.icatproject.dashboard.exposed.PredicateCreater.getEntityCountPredicate;
import static org.icatproject.dashboard.exposed.PredicateCreater.getInstrumentPredicate;
import org.icatproject.dashboard.manager.DashboardSessionManager;
import static org.icatproject.dashboard.utility.RestUtility.convertResultsToJson;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.IcatDataManager;
import org.icatproject.dashboard.manager.PropsManager;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDateTime;
import static org.icatproject.dashboard.utility.RestUtility.createPrePopulatedLongMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Path("/icat")
public class IcatRest {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DashboardSessionManager.class);

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;
    
    @EJB
    IcatDataManager icatData;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;
    
    private final GeoLocation dummyLocation = new GeoLocation(54.3739, 2.9376, "GB", "Windermere", "Dummy ISP");
    
    /**
     * Retrieves the authenticators that are used within the current ICAT group.
     * 
     * @return a JSONArray in the format of [{"mnemonic":"uows"},{"mnemonic":"ldap"},{"mnemonic":"simple"}]
     * 
     * @throws BadRequestException 
     * @throws AuthenticationException
     * @throws InternalException  
    
     * 
     * @statuscode 200 To indicate success
     
     */
    @GET
    @Path("authenticators")
    @Produces(MediaType.APPLICATION_JSON)
    public String getICATAuthenticators() throws DashboardException {       

        return icatData.getAuthenticators();
    }

    /**
     * Retrieves the ICAT logs.
     *
     * @param sessionID for authentication
     * @param queryConstraint any JPQL expression that can be appended to "SELECT download from Download download", e.g. "where download.id = 10".
     * @param initialLimit the initial limit value. Similar to LIMIT in SQL with initial Limit being the first value.
     * @param maxLimit the end limit value.  Similar to LIMIT in SQL with max Limit being the second value.
     * @return a JSON array of ICAT Log JSON Objects in the format of [{"duration":9,"entityType":"Investigation","query":"Investigation AS Investigation$ INCLUDE Investigation$.type AS InvestigationType_$, Investigation$.investigationInstruments AS InvestigationInstrument_$, InvestigationInstrument_$.instrument AS Instrument_$, Investigation$.publications AS Publication_$, Investigation$.investigationUsers AS InvestigationUser_$, InvestigationUser_$.user AS User_$","ipAddress":null,"fullName":"Mr ICAT DOI Reader","entityId":15071364,"id":4093564351,"operation":"get","logTime":"2014-01-29T15:49:30"}]
     * 
     * @throws BadRequestException      
     * @throws AuthenticationException
     * @throws InternalException
   
     * 
     * @statuscode 200 To indicate success
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

        String query = "SELECT log, user from ICATLog log JOIN log.user user ";

        //Check status of passed paramaters and build query.		
        if (!("".equals(queryConstraint))) {
            query += queryConstraint;
        }

        List<Object[]> logs = manager.createQuery(query).setFirstResult(initialLimit).setMaxResults(maxLimit).getResultList();

        JSONArray result = new JSONArray();

        for (Object[] log : logs) {
            JSONObject obj = new JSONObject();
            ICATLog tempLog = (ICATLog) log[0];
            ICATUser user = (ICATUser) log[1];
            obj.put("fullName", user.getFullName());
            obj.put("id", tempLog.getId());
            obj.put("entityId", tempLog.getEntityId());
            obj.put("entityType", tempLog.getEntityType());
            obj.put("ipAddress", tempLog.getIpAddress());
            obj.put("logTime", convertToLocalDateTime(tempLog.getLogTime()).toString());
            obj.put("operation", tempLog.getOperation());
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
     * @return a JSON in the format of [{"number":1,"city":"Abingdon","countryCode":"United Kingdom","latitude":51.6711,"isp":"Science and Technology Facilites Council","longitude":-1.2828}]
     * 
     * @throws BadRequestException  
     * @throws AuthenticationException
     * @throws InternalException
     
    
     * 
     * @statuscode 200 To indicate success
     */
    @GET
    @Path("logs/location")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIcatLogLocation(@QueryParam("sessionID") String sessionID,
                                     @QueryParam("logId") Long logId) throws DashboardException {

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
    
    /***
     * Retrieves the name of instruments in the ICAT
     * @return A JSONArray of JSONObjects {name:"AlF"}.
     */
    @GET
    @Path("instrument/names")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInstrumentNames(){       
        
        JSONArray instrumentNames = new JSONArray();
        
        LinkedHashMap<String, Long> instrumentIds = icatData.getInstrumentIdMapping();
        
        for(String value : instrumentIds.keySet()){
            JSONObject obj = new JSONObject();
            obj.put("name", value);
            instrumentNames.add(obj);
        }        
        
        
        return instrumentNames.toJSONString();
    }
    
    /**
     * Retrieves the number of entities inserted into the ICAT for each day between the start 
     * and end date.
     * @param entity to search for. e.g. Datafile
     * @param sessionID for authentication.
     * @param startDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @return a JSONArray containing JSONObjects of [{date:2015-01-20, number:200}]
     * 
     * @throws BadRequestException  
     * @throws AuthenticationException 
    
     * 
     * @statuscode 200 To indicate success
     
     */
    @GET
    @Path("{entity}/number")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEntityCount(@PathParam("entity") String entity,
                                 @QueryParam("sessionID") String sessionID,
                                 @QueryParam("startDate") String startDate,
                                 @QueryParam("endDate") String endDate) throws BadRequestException, AuthenticationException{
        
        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }         
        
        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));   
        
        TreeMap<LocalDate,Long> dateMap = createPrePopulatedLongMap(convertToLocalDate(start), convertToLocalDate(end));
      
        
        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<EntityCount> entityCount = query.from(EntityCount.class);         
        
        Predicate finalPredicate = getEntityCountPredicate(cb, entityCount, start,end,entity);
        
        query.multiselect(entityCount.<Date>get("countDate"), entityCount.<Long>get("entityCount"));        
        
        query.where(finalPredicate); 
       
        
        List<Object[]> result = manager.createQuery(query).getResultList();

        return convertResultsToJson(result,dateMap);
        
        
        
    }
    
    /**
     * Retrieves a list of entities that have been counted from the ICAT.
     * @return A JSONArray of entity objects e.g. {name:"DATAFILE"}.
     */
    @GET
    @Path("entity/name")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEntities(){
        
        String entityQuery = "SELECT DISTINCT(entity.entityType) FROM EntityCount as entity ORDER BY entity.entityType ASC";
        

        List<Object> entities = manager.createQuery(entityQuery).getResultList();
        
        JSONArray entityArray = new JSONArray();
        
        for(Object entity : entities){
            JSONObject obj = new JSONObject();
            obj.put("name", entity);
            entityArray.add(obj);
        }
        
        return entityArray.toJSONString();

        
    }
    
    
    /**
     * Retrieves the volume of investigations in order of highest volume.
     * 
     * @param sessionID for authentication.
     * @param startDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param limit the top list of investigations e.g. 10 will return top 10.
     * @return a JSONArray containing JSONObjects in the form of [{"investigationId":79110229,"value":289},{"investigationId":81650413,"value":24}]
     * 
     * @throws BadRequestException  
     * @throws AuthenticationException 
    
     * 
     * @statuscode 200 To indicate success
     
     */
    @GET
    @Path("investigation/datafile/volume")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInvestigationDatafileVolume(
                                 @QueryParam("sessionID") String sessionID,
                                 @QueryParam("startDate") String startDate,
                                 @QueryParam("endDate") String endDate,
                                 @QueryParam("limit")int limit) throws BadRequestException, AuthenticationException{
        
        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }      
        
                
        return getInvestigationMetaData("datafileVolume",startDate,endDate,limit); 
    }
     
    
    /**
     * Retrieves the number of datafiles per investigations in order of the highest number.
     * 
     * @param sessionID for authentication.
     * @param startDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param endDate to search from in the form of a Unix timestamp in milliseconds e.g. 1465254000661.
     * @param limit the top list of investigations e.g. 10 will return top 10.
     * @return a JSONArray containing JSONObjects in the form of [{"investigationId":79110229,"value":289},{"investigationId":81650413,"value":24}]
     * 
     * @throws BadRequestException  
     * @throws AuthenticationException 
    
     * 
     * @statuscode 200 To indicate success
     
     */
    @GET
    @Path("investigation/datafile/number")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInvestigationDatafileNumber(
                                 @QueryParam("sessionID") String sessionID,
                                 @QueryParam("startDate") String startDate,
                                 @QueryParam("endDate") String endDate,
                                 @QueryParam("limit")int limit) throws BadRequestException, AuthenticationException{
        
        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }      
        
                
        return getInvestigationMetaData("datafileCount",startDate,endDate,limit); 
    }
    
    /**
     * Retrieves the volume of datafiles created on a day to day basis between
     * the two dates provided.
     * @param sessionID for authentication.
     * @param startDate to search from.
     * @param endDate to search to.
     * @return a JSONArray containing JSONObjects with date and value pairs in the form of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":195676447}]
     * 
     * @throws BadRequestException 
     * @throws AuthenticationException
  
     * 
     * @statuscode 200 To indicate success 
     */
    @GET
    @Path("datafile/volume")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDatafileNumber(
                                 @QueryParam("sessionID") String sessionID,
                                 @QueryParam("startDate") String startDate,
                                 @QueryParam("endDate") String endDate) throws BadRequestException, AuthenticationException{
        
        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        } 
        
        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));   
        
        TreeMap<LocalDate,Long> dateMap = createPrePopulatedLongMap(convertToLocalDate(start), convertToLocalDate(end));     
        
        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvestigationMetaData> datafileVolume = query.from(InvestigationMetaData.class);  
        
        Predicate dateRange = getDatePredicate(cb,datafileVolume, start,end,"collectionDate");   
        
        query.multiselect(datafileVolume.<Date>get("collectionDate"),cb.sum(datafileVolume.<Long>get("datafileVolume")));

        query.groupBy(datafileVolume.<Date>get("collectionDate"));
        
        query.where(dateRange);      
        
        List<Object[]> result = manager.createQuery(query).getResultList();
        
        return convertResultsToJson(result,dateMap);
        
    }
    

    /**
     * Retrieves the number of datafiles created on a day to day basis between
     * the two dates provided and on the instrument provided.
     * @param sessionID for authentication.
     * @param instrument name of the instrument.
     * @param startDate to search from.
     * @param endDate to search to.
     * @return a JSONArray containing JSONObjects with date and value pairs in the form of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":195676447}]
     * 
     * @throws BadRequestException 
     * @throws AuthenticationException
  
     * 
     * @statuscode 200 To indicate success 
     */
    @GET
    @Path("{instrument}/datafile/number")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInstrumentDataFileCount(@QueryParam("sessionID") String sessionID,
                                    @PathParam("instrument")final String instrument,
                                    @QueryParam("startDate") String startDate,
                                    @QueryParam("endDate") String endDate) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }
        
        return getInstrumentMetaData("datafileCount", startDate, endDate, instrument);
    }
    
    /**
     * Retrieves the volume of datafiles created on a day to day basis between
     * the two dates provided and on the instrument provided.
     * @param sessionID for authentication.
     * @param instrument name of the instrument.
     * @param startDate to search from.
     * @param endDate to search to.
     * @return a JSONArray containing JSONObjects with date and value pairs in the form of [{"date":"2016-06-07","number":0},{"date":"2016-06-08","number":195676447}]
     * 
     * @throws BadRequestException 
     * @throws AuthenticationException
  
     * 
     * @statuscode 200 To indicate success 
     */
    @GET
    @Path("{instrument}/datafile/volume")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInstrumentDataFileVolume(@QueryParam("sessionID") String sessionID,
                                    @PathParam("instrument")final String instrument,
                                    @QueryParam("startDate") String startDate,
                                    @QueryParam("endDate") String endDate) throws DashboardException {

        if (sessionID == null) {
            throw new BadRequestException("sessionID must be provided");
        }
        if (!(beanManager.checkSessionID(sessionID, manager))) {
            throw new AuthenticationException("An invalid sessionID has been provided");
        }
        
      
        
        return getInstrumentMetaData("datafileVolume", startDate, endDate, instrument);
    }
    /**
     * Gets the geolocation of an ICATLog.
     *
     * @param logId the id of the log.
     * @return A geoLocation object of where the Log took place.
     */
    private GeoLocation getLogLocation(Long logId) {

        String locationQuery = "SELECT location from GeoLocation location JOIN location.logs log WHERE log.id='" + logId + "'";
        String ipQuery = "SELECT log.ipAddress FROM ICATLog log WHERE log.id='" + logId + "'";

        List<Object> location = manager.createQuery(locationQuery).getResultList();
        GeoLocation geoLocation;

        /* Location has not been set due to it being a functional account log. We do not store that to prevent
         * the geoLocation API blocking the dashboards ip.
         */
        if (location.isEmpty()) {
            List<Object> ipList = manager.createQuery(ipQuery).getResultList();
            
            try {
                geoLocation = GeoTool.getGeoLocation((String) ipList.get(0), manager, beanManager);
            }
            catch (GetLocationException ex) {
                /* Finding the location has failed. Must set to the dummy location to make sure the download is still added to Dashboard.
                 * Don't need to create a bean manager for this as it's only a dummy value anyway.
                */
                LOG.error(ex.getMessage() + " ipAddress: " + ex.getIpAddress());
                geoLocation = dummyLocation;
            }
            
        } else {
            geoLocation = (GeoLocation) location.get(0);
        }

        return geoLocation;
    }  
  
   
   /**
    * Creates and executes a query on the investigationMeta entity. It gathers the
    * datafile count and volume between the provided dates and up to a limit.
    * @param type datafile volume or count.
    * @param startDate to search from.
    * @param endDate to search up to.
    * @param limit of how many investigations to search up to.
    * @return a JSONArray of JSONObjects each containing an investigation id and either the volume or number of data files between the set period.
    */ 
    private String getInvestigationMetaData(String type, String startDate, String endDate, int limit){
        
        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));       
        
        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InvestigationMetaData> investigationMeta = query.from(InvestigationMetaData.class);        
        
        Predicate dateRange = getDatePredicate(cb,investigationMeta,start,end,"collectionDate"); 
        
        
        query.multiselect(investigationMeta.<Long>get("investigationId"), cb.sum(investigationMeta.<Long>get(type)));        
        
        query.where(dateRange);
        query.groupBy(investigationMeta.get("investigationId"));
        
        query.orderBy(cb.desc(cb.sum(investigationMeta.<Long>get(type))));
        
        List<Object[]> result = manager.createQuery(query).setFirstResult(0).setMaxResults(limit).getResultList();
        
        JSONArray resultArray =  new JSONArray();
        
        for(Object[] investigation: result){
            JSONObject obj = new JSONObject();
            obj.put("investigationId",investigation[0]);
            obj.put("value",investigation[1]);
            resultArray.add(obj);
            
        }
        
        return resultArray.toJSONString();
    }
    
    /**
    * Creates and executes a query on the instrumentMeta entity. It gathers the
    * datafile count and volume between the provided dates and for the specified instrument.
    * @param type datafile volume or count.
    * @param startDate to search from.
    * @param endDate to search up to.
    * @param instrument to search for.
    * @return a JSONArray of JSONObjects each containing an instrument name and either the volume or number of data files between the set period.
    */
    private String getInstrumentMetaData(String type, String startDate, String endDate, String instrument){      
        
        Date start = new Date(Long.valueOf(startDate));
        Date end = new Date(Long.valueOf(endDate));

        TreeMap<LocalDate,Long> dateMap = createPrePopulatedLongMap(convertToLocalDate(start), convertToLocalDate(end));     
        
        String instrumentId = String.valueOf(icatData.getInstrumentIdMapping().get(instrument));
      
        if (instrumentId.equals("null")) {
            instrumentId = "1";
        }
        
        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<InstrumentMetaData> instrumentMeta = query.from(InstrumentMetaData.class);      
       
        query.multiselect(instrumentMeta.<Date>get("collectionDate"), instrumentMeta.<Long>get(type));  
        
        Predicate finalPredicate = getInstrumentPredicate(cb,instrumentMeta,start,end,instrumentId,"collectionDate");
        
        query.where(finalPredicate);    
       
        
        List<Object[]> result = manager.createQuery(query).getResultList();
        
        return convertResultsToJson(result,dateMap);
        
    }

}
