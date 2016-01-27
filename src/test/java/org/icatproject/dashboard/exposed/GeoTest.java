/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.icatproject.dashboard.entity.DownloadLocation;
import org.icatproject.dashboard.tools.GeoTool;


public class GeoTest {

    /**
     * If there is a failure at any point then null will be returned so if it isn't null
     * then it must of passed.
     */  
    @Test
    public void downloadLocationShouldNotBeNull(){
       DownloadLocation dl = null;
       String ipAddress = "130.246.132.178";
       
       dl = GeoTool.getDownloadLocation(ipAddress);
       
       
       assertTrue(dl!=null);
       
    }
}
