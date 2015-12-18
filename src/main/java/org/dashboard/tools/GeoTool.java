
package org.dashboard.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dashboard.core.entity.DownloadLocation;
import org.dashboard.core.entity.UserLocation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is used as a module for getting location
 * 
 */
public class GeoTool {
 
    //End point of the API
    private static String apiEndPoint = "http://ipinfo.io/"; 
    
    /**
     * Gets the longitude and latitude from the above API and inserts it into a DownloadLocation object.
     * @param ipAddress
     * @return DownloadLocation object with its filled in variables.
     */    
    public static DownloadLocation getDownloadLocation(String ipAddress) {
        DownloadLocation dl = null;
        JSONObject result;
        JSONParser parser = new JSONParser();
        
        try {
            dl = new DownloadLocation();   
            
            URL url = new URL(apiEndPoint + ipAddress + "/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = rd.readLine()) != null) {
                sb.append(line);
                
            }                      
            rd.close();
            conn.disconnect();
            
            result = (JSONObject) parser.parse(sb.toString());
            
            dl.setHostMachineName((String) result.get("hostname"));
            String[] loc = ((String)result.get("loc")).split(",");
                       
            
            double longitude= Double.parseDouble(loc[0]);
            double latitude = Double.parseDouble(loc[1]);
                         
            dl.setLatitude(latitude);
            dl.setLongitude(longitude);
            dl.setIpAddress(ipAddress);
            
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dl;
        
    }
    
    /**
     * Gets the longitude and latitude from the above API and inserts it into a UserLocation object.
     * @param ipAddress
     * @return 
     */
    public static UserLocation getUserLocation(String ipAddress){
        UserLocation ul = null;
        JSONObject result;
        JSONParser parser = new JSONParser();
        
        try {
            ul = new UserLocation();   
            
            URL url = new URL(apiEndPoint + ipAddress + "/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = rd.readLine()) != null) {
                sb.append(line);
                
            }                      
            rd.close();
            conn.disconnect();
            
            result = (JSONObject) parser.parse(sb.toString());
            
            ul.setHostMachineName((String) result.get("hostname"));
            String[] loc = ((String)result.get("loc")).split(",");
                       
            
            double longitude= Double.parseDouble(loc[0]);
            double latitude = Double.parseDouble(loc[1]);
                         
            ul.setLatitude(latitude);
            ul.setLongitude(longitude);
            
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ul;
    }
    
    
    
}
