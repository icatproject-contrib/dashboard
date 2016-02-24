
package org.icatproject.dashboard.consumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.icatproject.dashboard.entity.DownloadLocation;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is used as a module for getting location
 * 
 */
public class GeoTool {
 
    //End point of the API
    private static final String apiEndPoint = "http://ip-api.com/json/"; 
    
    /**
     * Gets the longitude and latitude from the above API and inserts it into a DownloadLocation object.
     * @param ipAddress
     * @param manager
     * @param beanManager
     * @return DownloadLocation object with its filled in variables.
     */    
    public static DownloadLocation getDownloadLocation(String ipAddress, EntityManager manager, EntityBeanManager beanManager) {
        DownloadLocation dl = null;
        JSONObject result;
        JSONParser parser = new JSONParser();
        
        try {
            dl = new DownloadLocation();   
            
            URL url = new URL(apiEndPoint + ipAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            StringBuilder sb;
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                    
                }
            }
            conn.disconnect();
            
            result = (JSONObject) parser.parse(sb.toString());   
           
            
                       
            double latitude = (double) result.get("lat");
            double longitude= (double) result.get("lon");
            String city = (String) result.get("city");
            String countryCode = (String) result.get("country");
            String isp = (String) result.get("isp");
            
            List<DownloadLocation> locations;
            
            locations = manager.createNamedQuery("DownloadLocation.check").setParameter("longitude", longitude).setParameter("latitude", latitude).getResultList();
            
            if(locations.size()>0){
                dl = locations.get(0);
            }
            else{
                
                dl.setCountryCode(countryCode);
                dl.setCity(city);
                dl.setLatitude(latitude);
                dl.setLongitude(longitude);
                dl.setIsp(isp);
                
                try {
                    beanManager.create(dl, manager);
                } catch (DashboardException ex) {
                    Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }                        
            
           
            
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dl;
        
    }
    
 
    
    
}
