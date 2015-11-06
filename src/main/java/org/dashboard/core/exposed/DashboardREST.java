/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.exposed;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.log4j.Logger;
import org.dashboard.core.manager.DashboardException;
import org.dashboard.core.manager.DashboardException.DashboardExceptionType;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.PropsManager;
import org.icatproject.icat.client.ICAT;
import org.icatproject.icat.client.IcatException;
import org.icatproject.icat.client.Session;

@Path("/")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DashboardREST {
    
    @EJB
    EntityBeanManager beanManager;
    
    @EJB 
    PropsManager properties;
    
    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;
    
    @Resource
    private UserTransaction userTransaction;
   
    private static Logger logger = Logger.getLogger(DashboardREST.class);
    
   
	@GET
	@Path("userInfo/login/{loggedIn}")
	@Produces("MediaType.Application_JSON")
	public String getUsersLogInfo(@PathParam("loggedIn")String loggedIn,@QueryParam("sessionID")String sessionID, @QueryParam("userName")String userName,@QueryParam("startDate")LocalDate startDate,@QueryParam("endDate")LocalDate endDate) throws DashboardException{
            if(sessionID == null){
                throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "sessionID must be provided");
            }
            String queryBuilder = "SELECT u FROM ICATUser u";
            beanManager.search("SELECT ", manager);
		
	}
	
	
	@GET
	@Path("userInfo/location")
	@Produces("MediaType.Application_JSON")
	public String getLocation(@QueryParam("sessionID")String sessionID,@QueryParam("userName")String userName){
            return null;
		
	
	
        }
   
        @POST
        @Path("session/login")
        public String login(@FormParam("json") String loginString) throws DashboardException, URISyntaxException, IcatException{
            if (loginString == null) {
			throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "json must not be null");
		}
            
            ICAT icat = new ICAT(properties.getICATUrl());
            Map<String, String> credentials = new HashMap<>();
            String plugin = null;
            String userName = null;
            try (JsonParser parser = Json.createParser(new ByteArrayInputStream(loginString.getBytes()))) {
                String key = null;
			boolean inCredentials = false;

			while (parser.hasNext()) {
				JsonParser.Event event = parser.next();
				if (event == JsonParser.Event.KEY_NAME) {
					key = parser.getString();
				} else if (event == JsonParser.Event.VALUE_STRING) {
					if (inCredentials) {
						credentials.put(key, parser.getString());
					} else {
						if (key.equals("plugin")) {
							plugin = parser.getString();
						}
					}
				} else if (event == JsonParser.Event.START_ARRAY && key.equals("credentials")) {
					inCredentials = true;
				} else if (event == JsonParser.Event.END_ARRAY) {
					inCredentials = false;
				}
			}
            }            
            
            Session session = icat.login(plugin, credentials);
            
            userName = session.getUserName();
            
            
        return null;

        }


        @DELETE
        @Path("session/logout")
        public String logut(){
            return null;

            }


        @GET
        @Path("entity/count")
        @Produces("MediaType.Application_JSON")
        public String getEntityCount(@QueryParam("sessionID")String sessionID,@QueryParam("EntityType")Entity type,@QueryParam("startDate")LocalDate startDate,@QueryParam("endDate")LocalDate endDate){
            return null;

        }

        @GET
        @Path("download/route")
        @Produces("MediaType.Application_JSON")
        public String getRoutes(){
            return null;

        }

        @GET
        @Path("download/age")
        @Produces("MediaType.Application_JSON")
        public String getFileAge(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")DateTime startDate,@QueryParam("endDate")DateTime endDate){
            return null;

        }

        @GET
        @Path("download/frequency")
        @Produces("MediaType.Application_JSON")
        public String getFrequent(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")LocalDate startDate,@QueryParam("endDate")LocalDate endDate){return null;
}


        @GET 
        @Path("download/size")
        @Produces("MediaType.Application_JSON")
        public String getSize(@QueryParam("sessionID")String sessionID,@QueryParam("Username")String userName,@QueryParam("startDate")LocalDate startDate,@QueryParam("endDate")LocalDate endDate){
            return null;

        }


    }	
}
