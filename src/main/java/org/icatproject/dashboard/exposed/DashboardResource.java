/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.net.URISyntaxException;
import java.util.HashMap;
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
@Path("")
public class DashboardResource {

    private String icatURL;

    @EJB
    EntityBeanManager beanManager;

    @EJB
    PropsManager properties;

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardResource.class);

    @PostConstruct
    public void init() {
        icatURL = properties.getICATUrl();
    }

    /**
     * Post login to the dashboard. Authentication is done via the ICAT.
     *
     * @param login Login object containing the authenticator, username and
     * password.
     * @return Session ID.
     * @throws URISyntaxException Incorrect ICAT URL provided.
     * @throws BadRequestException Username, authenticator or password is
     * missing.
     */
    @POST
    @Path("session/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public String login(Login login) throws DashboardException, URISyntaxException {

        try {
            if (login.getAuthenticator() == null) {
                throw new BadRequestException(" authenticator type must be provided");
            }
            if (login.getUsername() == null) {
                throw new BadRequestException(" username must be provided");
            }
            if (login.getPassword() == null) {
                throw new BadRequestException(" password must be provided");
            }

            ICAT icat = new ICAT(icatURL);
            Map<String, String> credentials = new HashMap<>();

            List<String> authorisedAccounts = properties.getAuthorisedAccounts();

            String user;
            String sessionID;
            JSONObject obj = new JSONObject();
            String authenticator;

            credentials.put("username", login.getUsername());
            credentials.put("password", login.getPassword());
            authenticator = login.getAuthenticator();

            Session session = icat.login(authenticator, credentials);

            user = session.getUserName();
            session.logout();

            for (String account : authorisedAccounts) {
                if (account.trim().contains(user)) {
                    sessionID = beanManager.login(user, 120, manager);
                    obj.put("sessionID", sessionID);
                    return obj.toString();
                }
            }

            throw new ForbiddenException("Access Denied");
        } catch (IcatException ex) {
            throw new org.icatproject.dashboard.exceptions.IcatException(ex.getMessage());
        }

    }

    /**
     * Deletes the sessionID in the dashboard to log the user out.
     *
     * @param sessionID SessionID to be deleted.
     * @return Logout Successful message.
     * @throws DashboardException Unable to find the sessionID.
     */
    @DELETE
    @Path("session/logout")
    public String logut(@QueryParam("sessionID") String sessionID) throws DashboardException {

        beanManager.logout(sessionID, manager);
        JSONObject obj = new JSONObject();
        obj.put("Logout", "Successful");

        return obj.toString();
    }


    /**
     * *
     * Simple ping to see if the dashboard is up and running.
     *
     * @return Message saying it is fine.
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "The Dashboard is doing fine!";
    }

}