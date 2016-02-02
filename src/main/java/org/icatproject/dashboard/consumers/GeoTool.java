
package org.icatproject.dashboard.consumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.icatproject.dashboard.entity.DownloadLocation;
import org.icatproject.dashboard.entity.UserLocation;
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
    private static String apiEndPoint = "http://ipinfo.io/"; 
    
    /**
     * Gets the longitude and latitude from the above API and inserts it into a DownloadLocation object.
     * @param ipAddress
     * @return DownloadLocation object with its filled in variables.
     */    
    public static DownloadLocation getDownloadLocation(String ipAddress, EntityManager manager, EntityBeanManager beanManager) {
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
            
           
            String[] loc = ((String)result.get("loc")).split(",");
                       
            double latitude = Double.parseDouble(loc[0]);
            double longitude= Double.parseDouble(loc[1]);
            String city = (String) result.get("city");
            String countryCode = (String) result.get("country");
            
            List<DownloadLocation> locations = new ArrayList<DownloadLocation>();
            
            locations = manager.createNamedQuery("DownloadLocation.check").setParameter("longitude", longitude).setParameter("latitude", latitude).getResultList();
            
            if(locations.size()>0){
                dl = locations.get(0);
            }
            else{
                
                dl.setCountryCode(countryCode);
                dl.setCity(city);
                dl.setLatitude(latitude);
                dl.setLongitude(longitude);
                
                try {
                    beanManager.create(dl, manager);
                } catch (DashboardException ex) {
                    Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }                        
            
           
            
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dl;
        
    }
    
 
    
    
}
