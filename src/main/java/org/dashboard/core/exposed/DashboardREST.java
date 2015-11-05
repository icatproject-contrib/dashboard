/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.exposed;

import java.time.LocalDate;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
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
        public String login(@FormParam("json") String loginString) throws DashboardException{
            if (loginString == null) {
			throw new DashboardException(DashboardExceptionType.BAD_PARAMETER, "json must not be null");
		}
            

        }


        @POST
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
