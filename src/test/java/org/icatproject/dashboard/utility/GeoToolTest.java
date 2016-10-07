/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates the template in the editor.
 */
package org.icatproject.dashboard.utility;


import java.util.List;
import java.util.ArrayList;
import javax.jms.Connection;
import org.icatproject.dashboard.consumers.GeoTool;
import static org.mockito.Mockito.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.icatproject.dashboard.consumers.DownloadListener;

import org.icatproject.dashboard.exceptions.GetLocationException;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.manager.EntityBeanManager;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author Tom Gowland
 */

/*
This will test various aspects of the GeoTool class which primarily deals with getting the locations of a users download.

The class uses Mockito (http://mockito.org/) as a Unit Test framework to mock various persistence aspects including the entity manager.
*/

public class GeoToolTest {
    
    
    private DownloadListener listener = new DownloadListener();
    
    private EntityManager entityManager;
    private Query query;
    private EntityBeanManager beanManager;
    private Connection connection;
    
    @Before
    public void setup() {
        // Here we initialise some of the peristence aspects which need to be mocked. 
        entityManager = mock(EntityManager.class);
        query = mock(Query.class);
        beanManager = mock(EntityBeanManager.class);
        connection = mock(Connection.class);
        
        List<GeoLocation> locations = new ArrayList<>();
        
        when(entityManager.createNamedQuery("GeoLocation.ipCheck")).thenReturn(query);
        when(query.setParameter("ipAddress", "1")).thenReturn(query);
        when(query.getResultList()).thenReturn(locations);
        
        listener.setManagers(entityManager, beanManager); 
    }
    
    /*
    The following test ensures that when an ipAddress of 127.0.0.1 is sent to GeoTool that it is converted to http://ip-api.com/json/ so 
    that a location can still be read properly. The test would only return a value if the IP is changed to http://ip-api.com/json/ else it
    will throw a nullpointerexception and fail. 
    */
    @Test
    public void testLocalIp() {
        String ipAddress = "127.0.0.1";
        String otherIp = "";
        
        List<GeoLocation> locations = new ArrayList<>();
        
        GeoLocation geoLocation = new GeoLocation();
        
        locations.add(geoLocation);
        
        /*
        The when method (http://www.baeldung.com/mockito-behavior) can be used to create the responses of queries to our mock database.
        Here, we mimic the same query that is called in the getGeoLocation method in GeoTool.java. In this way, when we call that method
        later on in the test, the class will use the following when statements to execute the query. We return an example list of locations
        which I have added a test location to so that we can make sure it is returned later on. This same principle is used in the tests 
        subsequent to this one. 
        */
        
        when(entityManager.createNamedQuery("GeoLocation.ipCheck")).thenReturn(query);
        when(query.setParameter("ipAddress", otherIp)).thenReturn(query);
        when(query.getResultList()).thenReturn(locations);

        GeoLocation location = new GeoLocation();
        
        try {
            GeoLocation testLocation;
            /*
            Finally, we access the getGeoLocation method with the 127.0.0.1 IP address and ensure that the result returned is the one we entered 
            into the list earlier. If the test passes, this tells us that the IP address has been successfully changed over to http://ip-api.com/json/.
            */
            testLocation = GeoTool.getGeoLocation(ipAddress, entityManager, beanManager);
            assertEquals(testLocation, geoLocation);
        }
        catch (GetLocationException e) {
            // Reaching here means the test has failed. The exception should have been dealt with by this point.
        }
    }

    /*
    The following test will attempt to request information from the ip API 155 times. If the algorithm in GeoTool didn't work, this test would fail
    as the IP address would be banned at the 151st request. The test takes quite a long time to run as the algorithm needs to wait for a minute until
    it can access the API again. Unfortunately, there isn't a way around this if we're trying to test that the API isn't blocked. 
    */
    @Test
    public void testRequests() {
        String ipAddress = "";
        int count = 0;
        
        List<GeoLocation> locations = new ArrayList<>();
        
        when(entityManager.createNamedQuery("GeoLocation.ipCheck")).thenReturn(query);
        when(query.setParameter("ipAddress", ipAddress)).thenReturn(query);
        when(query.getResultList()).thenReturn(locations);
        
        when(entityManager.createNamedQuery("GeoLocation.check")).thenReturn(query);
        when(query.setParameter(eq("longitude"), anyDouble())).thenReturn(query);
        when(query.setParameter(eq("latitude"), anyDouble())).thenReturn(query);
        when(query.getResultList()).thenReturn(locations);
        
        // Access the getGeoLocation 155 times. 
        while(count < 154) {
            try {
                GeoTool.getGeoLocation(ipAddress, entityManager, beanManager);  
            }
            catch (GetLocationException e) {
                // If this exception is thrown, the test should fail.
            }
            count ++;
        }
    }

    /*
    The following test will send an invalid IP address to the getLocation method. This should mean that the location returned is set to a dummy
    location rather than ignoring the download completely. The test will access the getLocation method and then will check to see if the 
    GeoLocation that is returned is the dummy one which we are expecting. 
    */
    @Test
    public void testMessage() {
        
        // Pass through an IP address that getLocation cannot deal with (because the API gives an error).
        GeoLocation testLocation = listener.getLocation("1");
        
        // Get the latitude and longitude of the location that is returned.
        double latitude = testLocation.getLatitude();
        double longitude = testLocation.getLongitude();
        GeoLocation dummyLocation = new GeoLocation(54.3739, 2.9376, "GB", "Windermere", "Dummy ISP");
        
        // Ensure the latitude and longitude are the same as one another.
        assertEquals(latitude, dummyLocation.getLatitude(), 0.0);
        assertEquals(longitude, dummyLocation.getLongitude(), 0.0);
    }
}
