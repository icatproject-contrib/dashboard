/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.icatproject.dashboard.consumers.GeoTool;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATLog;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.exceptions.AuthenticationException;
import org.icatproject.dashboard.exceptions.BadRequestException;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.exceptions.ForbiddenException;
import org.icatproject.dashboard.exceptions.InternalException;
import static org.icatproject.dashboard.exposed.RestUtility.convertToLocalDateTime;
import org.icatproject.dashboard.manager.EntityBeanManager;
import org.icatproject.dashboard.manager.PropsManager;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
