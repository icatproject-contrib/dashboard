/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.utility;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


public class DateUtility {
    
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
    
    /**
     * Converts a LocalDate to a date
     * @param date to be converted.
     * @return a LocalDate of the date passed.
     */
    public static Date convertToDate(LocalDate date){
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
        
        
    
}
