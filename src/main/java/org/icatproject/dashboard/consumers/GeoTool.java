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
import java.util.stream.IntStream;

/**
 * This class is used as a module for getting location
 *
 */
public class GeoTool {

    //End point of the API
    private static final String APIENDPOINT = "http://ip-api.com/json/";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GeoTool.class);
    
    /* Set to 149 even though the limit is 150. This is just to compensate for any difference in calculations
       between the API and Dashboard due to rounding errors.
    */
    private static double limit = 149.0;

    public static LocalDateTime startTime;
	
    public static int[] cycles = new int [60];
    public static int[] requests = new int [60];

    /**
     * Gets the longitude and latitude from the above API and inserts it into a
     * DownloadLocation object.
     *
     * @param ipAddress
     * @param manager
     * @param beanManager
     * @throws GetLocationException
     * @return DownloadLocation object with its filled in variables.
     */
    public static GeoLocation getGeoLocation(String ipAddress, EntityManager manager, EntityBeanManager beanManager) throws GetLocationException {
        
        
        // An IP address of 127.0.0.1 will cause the program to crash. If you give the api no ip address, it will automatically generate information
        // for your local address. In this way, these errors can be avoided and useful information can still be recieved from the API.
        if (ipAddress.contains("127.0.0.1")) {
            ipAddress = "";
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
                throw new GetLocationException("Failed to get location", ipAddress);
            }
            
            // If the JSON contains this, something is wrong with the IP address so we need to throw the exception.
            if(result.containsValue("invalid query")) {
                throw new GetLocationException("Failed to get location", ipAddress);
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
                    throw new GetLocationException("Failed to get location", ipAddress);
                }  
            }   
                        
            //Add the GeoIpAddress to the GeoLocation.
            try {
                GeoIpAddress geoIp = new GeoIpAddress(location,ipAddress);
                beanManager.create(geoIp, manager);
            } catch (DashboardException ex) {
                LOG.error("Issue creating GeoIpAddress: " + ex);
                throw new GetLocationException("Failed to get location", ipAddress);
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
        
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
		
        LocalDateTime measureTime = LocalDateTime.now();

        double secondsPassed = ChronoUnit.SECONDS.between(startTime, measureTime);

        int roundedSeconds = (int) Math.round(secondsPassed);

        int moduloSeconds = roundedSeconds % 60;
        int divideSeconds = roundedSeconds / 60;
        
        int sum = sumArray(requests);
        
        if (cycles[moduloSeconds] != divideSeconds) {
            cycles[moduloSeconds] = divideSeconds;
            requests[moduloSeconds] = 0;
        }
        
        if (sum > limit) {
            try {
                Thread.sleep(1000);
                return contactAPI(ipAddress);
            }
            catch (InterruptedException e) {
                LOG.error("Thread sleep was interrupted " + e);
            }
        }
        
        else {
            if (cycles[moduloSeconds] == divideSeconds) {
			requests[moduloSeconds] += 1;
            }
            else {
                cycles[moduloSeconds] = divideSeconds;
                requests[moduloSeconds] = 1;
            }
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
                    throw new GetLocationException("Failed to get location", ipAddress);
                }
                conn.disconnect();
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) {
                    LOG.error("Thread sleep was interrupted ", ex);
                }
                return sb.toString();
            } catch (MalformedURLException ex) {
                LOG.error("Error has occured with processing the return for the GeoTool API ", ex);
            } catch (IOException ex) {
                LOG.error("Error has occured with processing the return for the GeoTool API ", ex);
            }
        }
        return null;
    }
    
    public static int sumArray (int [] array) {
        int count = 0;
        for (int a : array) {
            count += a;
        }
        return count;
    }

}
