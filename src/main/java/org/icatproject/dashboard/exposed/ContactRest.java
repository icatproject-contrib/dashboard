/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Path("/contact")
public class ContactRest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ContactRest.class);

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;


    /**
     * Retrieves the contact message from the properties file.
     *
     * @return The contact message string specified in the properties file.
     */
    @GET
    @Path("message")
    @Produces(MediaType.APPLICATION_JSON)
    public String getContactMessage(@QueryParam("sessionID") String sessionID) {
        JSONArray result = new JSONArray();
        JSONObject messageData = new JSONObject();
        messageData.put("message", properties.getContactMessage());
        result.add(messageData);
        return result.toJSONString();
    }
}
