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
import java.util.Map;
import java.util.TreeMap;
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
        JSONObject obj = new JSONObject();
        JSONArray ary = new JSONArray();
            
        for(Map.Entry<LocalDate,Long> entry : mapToBeConverted.entrySet()) {
            obj = new JSONObject();

            LocalDate key = entry.getKey();
            Long value = entry.getValue();

            obj.put("date",key.toString());
            obj.put("amount",value);

            ary.add(obj);

          }
        
        return ary;

    }
    
}
