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
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.icatproject.dashboard.entity.ICATUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



/**
 * Utility Class to deal with date and unit conversions.
 * 
 */
public class RestUtility {
    
    
    /***
     * Calculates the difference in hours between two Localdates.
     * @param start Earliest LocalDate
     * @param end Latest LocalDate
     * @return Difference between start and end in hours.
     */
    public static long calculateHourDifference(LocalDateTime start, LocalDateTime end){
        return Duration.between(start, end).toHours();
    }
    
    /***
     * Converts a Date to a LocalDate
     * @param date to be converted.
     * @return Localdate object value of the date provided.
     */
    public static LocalDate convertToLocalDate(Date date){
        Instant instant = Instant.ofEpochMilli(date.getTime());
        
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate(); 
        
        return  localDate;
    }
    
    /***
     * Converts a Date to LocalDateTime
     * @param date to be converted.
     * @return LocalDateTime object of the date provided.
     */
    public static LocalDateTime convertToLocalDateTime(Date date){
        
        
        Instant instant = Instant.ofEpochMilli(date.getTime());
        
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()); 
        
        return  localDateTime;
    }
    
    /***
     * Creates a TreeMap that contains the values of each date between the two dates provided.
     * @param startPoint Start date to populate up to.
     * @param endPoint End date to populate up to.
     * @return a TreeMap<LocalDate, Long> object.
     */
    public static TreeMap<LocalDate,Long> createPrePopulatedMap(LocalDate startPoint, LocalDate endPoint){
        
        TreeMap<LocalDate,Long> treeMap = new TreeMap<LocalDate,Long>();
        
        while(!startPoint.isAfter(endPoint)){
            treeMap.put(startPoint,new Long(0));
            startPoint = startPoint.plusDays(1);
        }
        
        return treeMap;
    } 
    
    /***
     * Pushes the TreeMap values into a JSONArray.
     * @param mapToBeConverted The map to have its valued converted to JSON
     * @return A JSON array with objects that have "date" and "value" keys.
     */
    public static JSONArray convertMapToJSON(TreeMap<LocalDate, Long> mapToBeConverted){
       
        JSONArray ary = new JSONArray();
            
        for(Map.Entry<LocalDate,Long> entry : mapToBeConverted.entrySet()) {
             JSONObject obj = new JSONObject();

            LocalDate key = entry.getKey();
            Long value = entry.getValue();

            obj.put("date",key.toString());
            obj.put("number",value);

            ary.add(obj);

          }
        
        return ary;

    }
    
     /**
         * Gets the full name of a user.
         * @param name of the user in the ICAT. This is the unique name e.g. uows/123456
         * @param manager
         * @return the full name of the user provided.
         */
        public static String getFullName(String name, EntityManager manager){
                 
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<String>  query = cb.createQuery(String.class);
            Root<ICATUser> user = query.from(ICATUser.class);     
            
            query.multiselect(user.get("fullName"));
            
            query.where(cb.equal(user.get("name"), name));
            
            String fullName = manager.createQuery(query).getSingleResult();
            
            
            return fullName;
        
        
        }
        
    /**
     * Converts a List results and inserts them into a treeMap. It then converts that into JSON to be sent
     * via the RESTFul API.
     * @param result is the list of objects
     * @param dateMap is the map of dates to have values assigned to.
     * @return a JSONArray of JSONObjects each with a date and value.
     */
    public static String convertResultsToJson(List<Object[]> result, TreeMap<LocalDate,Long> dateMap){
        
        for(Object[] day : result){
            LocalDate collectionDate = RestUtility.convertToLocalDate((Date) day[0]);
            
            dateMap.put(collectionDate, (Long) day[1]);
        }     

        return RestUtility.convertMapToJSON(dateMap).toJSONString();
    }    
          
    
}
