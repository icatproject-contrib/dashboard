package org.icatproject.dashboard.consumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.persistence.EntityManager;
import org.icatproject.dashboard.entity.GeoIpAddress;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.icatproject.dashboard.exceptions.GetLocationException;
import org.slf4j.LoggerFactory;

/**
 * This class is used as a module for getting location
 *
 */
public class GeoTool {

    //End point of the API
    private static final String APIENDPOINT = "http://ip-api.com/json/";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GeoTool.class);
    
    private static double rate = 150.0;
    private static double seconds = 60.0;
    
    private static double messageAllowance = rate;
    
    private static LocalDateTime lastCheckTime;

    /**
     * Gets the longitude and latitude from the above API and inserts it into a
     * DownloadLocation object.
     *
     * @param ipAddress
     * @param manager
     * @param beanManager
     * @return DownloadLocation object with its filled in variables.
     */
    public static GeoLocation getGeoLocation(String ipAddress, EntityManager manager, EntityBeanManager beanManager) throws GetLocationException {
        
        
        // An IP address of 127.0.0.1 will cause the program to crash. If you give the api no ip address, it will automatically generate information
        // for your local address. In this way, these errors can be avoided and useful information can still be recieved from the API.
        if (ipAddress.contains("127.0.0.1")) {
            ipAddress = "http://ip-api.com/json/";
        }

        GeoLocation location; 
        
        // Get the previous locations from the database (i.e locations that have already downloaded files)
        List<GeoLocation> locations = manager.createNamedQuery("GeoLocation.ipCheck").setParameter("ipAddress", ipAddress).getResultList();
        
        //If not found from the ipAddress then contact the API to get the long and latitude. 
        if(locations.isEmpty()) {
            
            JSONParser parser = new JSONParser();
            JSONObject result = new JSONObject();
            try {
                result = (JSONObject) parser.parse(contactAPI(ipAddress));
            } catch (ParseException ex) {
                LOG.error("Issue parsing JSON data", ex);
                throw new GetLocationException("Failed to get location for "  + ipAddress);
            } catch (GetLocationException ex) {
                throw new GetLocationException(ex.getShortMessage());
            }
            
            double latitude = (double) result.get("lat");
            double longitude = (double) result.get("lon");
            String city = (String) result.get("city");
            String countryCode = (String) result.get("country");
            String isp = (String) result.get("isp");
            
            locations = manager.createNamedQuery("GeoLocation.check").setParameter("longitude", longitude).setParameter("latitude", latitude).getResultList();
            if (locations.size() > 0) {
                location = locations.get(0);
            }
            else {
                location = new GeoLocation( longitude, latitude, countryCode, city,  isp);      
                
                try {
                    beanManager.create(location, manager);
                } catch (DashboardException ex) {
                    // Finding the location has failed. Must set to the dummy location to make sure the download is still added to Dashboard.
                    // Don't need to create a bean manager for this as it's only a dummy value anyway.
                    LOG.error(ex.getShortMessage());
                    throw new GetLocationException("Failed to get location for "  + ipAddress);
                }  
            }   
                        
            //Add the GeoIpAddress to the GeoLocation.
            try {
                GeoIpAddress geoIp = new GeoIpAddress(location,ipAddress);
                beanManager.create(geoIp, manager);
            } catch (DashboardException ex) {
                LOG.error("Issue creating GeoIpAddress: " + ex);
                throw new GetLocationException("Failed to get location for "  + ipAddress);
            }
        } 
        else {
            location = locations.get(0);
        }

        return location;

    }
    
 

    /**
     * Contacts the GeoLocation API to collect a geoLocation from an ipAddress
     *
     * @param ipAddress
     * @return
     */
    private static String contactAPI(String ipAddress) throws GetLocationException {
        /* Simple implementation of the token bucket algorithm to limit the number of requests to 150 per minute.
         * It works using a sliding window of time and enforces that a maximum of 150 requests to this method can 
         * be dealt with during that time. It should solve the issue of being blocked by the GeoTool API.
         */
        
        LocalDateTime nowTime = LocalDateTime.now();
        
        if (lastCheckTime == null) {
            lastCheckTime = LocalDateTime.now();
        }
            double secondsPassed = ChronoUnit.SECONDS.between(lastCheckTime, nowTime);
            lastCheckTime = nowTime;
            
            messageAllowance += secondsPassed * (rate / seconds);
            
            if (messageAllowance > rate) {
                messageAllowance = rate;
            }
            if (messageAllowance < 1.0) {
                try {
                    LOG.info("API recieved too many calls, waiting 10 seconds and then trying again");
                    Thread.sleep(10000);
                    contactAPI(ipAddress);
                }
                catch (InterruptedException ex) {
                    LOG.error("Thread sleep was interrupted ", ex);
                }
            }
            else {
                try {
                    URL url = new URL(APIENDPOINT + ipAddress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = null;
                    try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        sb = new StringBuilder();
                        String line;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);

                        }
                    } catch (IOException e) {
                        // This will usually happen when there are too many requests in one minute (more than 150)
                        LOG.error("Error has occured with contacting the GeoTool API ", e);
                        throw new GetLocationException("Failed to get location for "  + ipAddress);
                    }
                    conn.disconnect();
                    return sb.toString();
                } catch (MalformedURLException ex) {
                    LOG.error("Error has occured with processing the return for the GeoTool API ", ex);
                } catch (IOException ex) {
                    LOG.error("Error has occured with processing the return for the GeoTool API ", ex);
                }
            }
        return null;
    }

}
