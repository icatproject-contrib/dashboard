/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.ForbiddenException;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Path("/")
public class DashboardRest {
    
    private static final Logger LOG = LoggerFactory.getLogger(DashboardRest.class);

    private String icatURL;

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    
    @PostConstruct
    public void init() {
        icatURL = properties.getICATUrl();
    }

    /**
     * Post login to the dashboard. Authentication is done via the ICAT.
     *
     * @param login Login object containing the authenticator, username and
     * password.
     * @return Session ID in the format of {"sessionID":"279524ea-a627-45ca-a6df-9d527f023d41"}.
     * 
     * @throws BadRequestException    
     
    
     * 
     * @statuscode 200 To indicate success
     */
    @POST
    @Path("session/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login(Login login) throws DashboardException, URISyntaxException {

        try {
            /*
            if (login.getAuthenticator() == null) {
                throw new BadRequestException(" authenticator type must be provided");
            }
            */
            if (login.getUsername() == null) {
                throw new BadRequestException(" username must be provided");
            }
            if (login.getPassword() == null) {
                throw new BadRequestException(" password must be provided");
            }

            ICAT icat = new ICAT(icatURL);
            Map<String, String> credentials = new HashMap<>();

            HashSet authorisedAccounts = properties.getAuthorisedAccounts();

            String user;
            String sessionID;
            JSONObject obj = new JSONObject();
            String authenticator;

            credentials.put("username", login.getUsername());
            credentials.put("password", login.getPassword());
            // authenticator = login.getAuthenticator();
            authenticator = "uows";

            Session session = icat.login(authenticator, credentials);

            user = session.getUserName();
            session.logout();

            
            if (authorisedAccounts.contains(user)) {
                sessionID = beanManager.login(user, 120, manager);
                obj.put("sessionID", sessionID);
                return obj.toString();
            }
            

            throw new ForbiddenException("Access Denied");
        } catch (IcatException ex) {
            throw new org.icatproject.dashboard.exceptions.IcatException(ex.getMessage());
        }

    }

    /**
     * Deletes the sessionID in the dashboard to log the user out.
     *
     * @param sessionID to be deleted.
     * 
     * @return Logout Successful message.
     * 
     * @throws BadRequestException     
     
    
     * 
     * @statuscode 200 To indicate success
     */
    @DELETE
    @Path("session/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public String logut(@QueryParam("sessionID") String sessionID) throws DashboardException {

        beanManager.logout(sessionID, manager);
        JSONObject obj = new JSONObject();
        obj.put("Logout", "Successful");

        return obj.toString();
    }


    /**
     * *
     * Ping to see if the dashboard REST API is accessible.
     *   
     * 
     * @statuscode 200 To indicate success
     **/
    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "The Dashboard is doing fine!";
    }

}
