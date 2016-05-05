/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;


public class DateUtilityTest {
    
   
   
    
    
    @Test
    public void date_today_convert_to_today_localDate(){
        Date dateToday = new Date();
        
        LocalDate correctToday = LocalDate.now();
        
        LocalDate localToday = DateUtility.convertToLocalDate(dateToday);
        
        assertEquals(correctToday,localToday);
        
        
    }
    
    @Test 
    public void hour_difference_is_10(){
        LocalDateTime start = LocalDateTime.of(2016,Month.APRIL,23,02,20,23);
        LocalDateTime end = LocalDateTime.of(2016,Month.APRIL,23,12,20,23);
        
        long difference = DateUtility.calculateHourDifference(start,end);
        
        long correctDifference = 10;
        
        assertEquals(correctDifference,difference);
                
    }
    
    @Test 
    public void date_today_convert_to_today_localDateTime(){
        Date nowDate = new Date();
        LocalDateTime nowDateTime = LocalDateTime.now();
        
        LocalDateTime result = DateUtility.convertToLocalDateTime(nowDate);
        
        assertEquals(result,nowDateTime);
        
    }
   

   
}
