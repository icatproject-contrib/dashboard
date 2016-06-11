/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import org.icatproject.dashboard.utility.RestUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import static org.icatproject.dashboard.utility.DateUtility.calculateHourDifference;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDateTime;


/**
 * Class to help in the processing 
 */
public class DownloadSizeProcessor {
    
    private LocalDate startPoint;
    private LocalDate endPoint; 
    private TreeMap<LocalDate,Long> downloadDates;
    
   
    
    public DownloadSizeProcessor(LocalDate start, LocalDate end){
        
        startPoint = start;
        endPoint = end;
        
        downloadDates = RestUtility.createPrePopulatedLongMap(startPoint, endPoint);   
        
        
    }
    
    /**
     * Calculates how much data was hyperthetically downloaded each date for each
     * download and adds this to an overall treemap.
     * @param downloadList List of downloads to be processed.
     * 
     * @return TreeMap of how much data was downloaded at each date.
     */
    public TreeMap<LocalDate,Long> calculateDataDownloaded (List<Object[]> downloadList){
        
         for(Object[] download : downloadList){               
                
                LocalDate beginningDate = convertToLocalDate((Date) download[0]);
                LocalDate endDate = convertToLocalDate((Date) download[1]);
                
                long size = (long)download[2];
                
                //Early download means it started before the startpoint so no need to do datetime calculations.
                boolean earlyDownload = false;
                
                
                //If download only spans over one day just add its totalsize.
                if(beginningDate.isEqual(endDate)){
                    Long currentTotal = downloadDates.get(beginningDate);
                    downloadDates.put(beginningDate,currentTotal+=size);                    
                }
                else{
                    
                    LocalDateTime beginningTime = convertToLocalDateTime((Date) download[0]);
                    LocalDateTime endTime = convertToLocalDateTime((Date) download[1]); 
                
                    long durationInHours = calculateHourDifference(beginningTime, endTime);
                    long bytesPerHour = size/durationInHours;
                    long bytesPerDay = bytesPerHour*24;
                    
                    //Bring the date up the users start date.
                    while(beginningDate.isBefore(startPoint)){
                        beginningDate = beginningDate.plusDays(1);
                        earlyDownload = true;
                        
                    }
                    
                    if(!earlyDownload){
                        //Calculate the initial part day.
                        long beginingDatenumber = (24 - beginningTime.getHour())*bytesPerHour;
                        Long currentTotal = downloadDates.get(beginningDate);
                        downloadDates.put(beginningDate,currentTotal+=beginingDatenumber);  

                        //Increment the day as do not want to calculate it again.
                        beginningDate = beginningDate.plusDays(1);
                    }
                    
                    
                    //Iterate through the other days
                    while(beginningDate.isBefore(endDate)&&(!beginningDate.isAfter(endPoint))){
                        Long dateTotalSize = downloadDates.get(beginningDate);
                        downloadDates.put(beginningDate,dateTotalSize+=bytesPerDay); 
                        beginningDate = beginningDate.plusDays(1);
                    }
                    
                    if(endDate.isBefore(endPoint)){
                        //Clean up with the last date.
                        long endDatenumber = (24-(24-endTime.getHour()))*bytesPerHour;
                        Long dateTotalSize = downloadDates.get(endDate);
                        downloadDates.put(endDate,dateTotalSize+=endDatenumber); 
                    }
                    
                }
                    
        
        
       
         }
          return downloadDates;
    }
   
    
}
