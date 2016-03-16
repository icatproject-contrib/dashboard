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
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

/**
 * This class is used as a module for getting location
 *
 */
public class GeoTool {

    //End point of the API
    private static final String apiEndPoint = "http://ip-api.com/json/";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GeoTool.class);

    /**
     * Gets the longitude and latitude from the above API and inserts it into a
     * DownloadLocation object.
     *
     * @param ipAddress
     * @param manager
     * @param beanManager
     * @return DownloadLocation object with its filled in variables.
     */
    public static GeoLocation getGeoLocation(String ipAddress, EntityManager manager, EntityBeanManager beanManager) {

        GeoLocation gl;
        JSONObject result =null;

        JSONParser parser = new JSONParser();

        try {
            result = (JSONObject) parser.parse(contactAPI(ipAddress));
        } catch (ParseException ex) {
            logger.error("Issue parsing JSON data", ex);
        }

        gl = new GeoLocation();
        double latitude = (double) result.get("lat");
        double longitude = (double) result.get("lon");
        String city = (String) result.get("city");
        String countryCode = (String) result.get("country");
        String isp = (String) result.get("isp");
        List<GeoLocation> locations;
        locations = manager.createNamedQuery("GeoLocation.check").setParameter("longitude", longitude).setParameter("latitude", latitude).getResultList();
        if (locations.size() > 0) {
            gl = locations.get(0);
        } else {
            gl.setCountryCode(countryCode);
            gl.setCity(city);
            gl.setLatitude(latitude);
            gl.setLongitude(longitude);
            gl.setIsp(isp);

            try {
                beanManager.create(gl, manager);
            } catch (DashboardException ex) {
                Logger.getLogger(GeoTool.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return gl;

    }

    /**
     * Contacts the GeoLocation API to collect a geoLocation from an ipAddress
     *
     * @param ipAddress
     * @return
     */
    private static String contactAPI(String ipAddress) {

        try {

            URL url = new URL(apiEndPoint + ipAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            StringBuilder sb = null;
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);

                }
            } catch (IOException e) {
                logger.error("Error has occured with contacting the GeoTool API ", e);
            }
            conn.disconnect();

            return sb.toString();
        } catch (MalformedURLException ex) {
            logger.error("Error has occured with processing the return for the GeoTool API ", ex);
        } catch (IOException ex) {
            logger.error("Error has occured with processing the return for the GeoTool API ", ex);
        }

        return null;

    }

}
