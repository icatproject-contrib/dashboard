/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.collector;

import org.dashboard.core.manager.PropsManager;
import org.dashboard.core.manager.EntityBeanManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.dashboard.core.entity.CollectionType;
import org.dashboard.core.exceptions.InternalException;
import org.dashboard.core.manager.ICATSessionManager;
import org.icatproject.*;

@Singleton
@Startup
@DependsOn("ICATSessionManager")
public class DataCollector {

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;

    @EJB
    private PropsManager prop;

    @EJB
    private ICATSessionManager session;

    @EJB
    private EntityBeanManager beanManager;

    @EJB
    private EntityCounter counter;

    @EJB
    private UserCollector userCollector;

    protected ICAT icat;
    protected String sessionID;

    DateTimeFormatter format;

    private static final Logger log = Logger.getLogger(DataCollector.class);

    @Resource
    private TimerService timerService;
        
    /**
     * Init method is called once the EJB has been loaded. Does the initial
     * property collections and login into ICAT. Also initiates initial data
     * collection.
     */
    @PostConstruct
    private void init() {
        log.info("Initial Setup Check.");

        createTimers(prop);
        icat = createICATLink();
        sessionID = session.getSessionID();        
        try {
            collectData();
        } catch (InternalException ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void collectData() throws InternalException {
        setupUserCollection();
        // setupEntityCollection();   

    }

    private void createTimers(PropsManager properties) {

        TimerConfig dataCollect = new TimerConfig("dataCollect", false);
        timerService.createCalendarTimer(new ScheduleExpression().hour(properties.getCollectTime()), dataCollect);

    }

    /**
     * Handles the timers. If statement inside decides what timer was called and
     * what method should be invoked to deal with that timer. Currently only
     * refresh session and collection of data.
     *
     * @param timer is the object that is invoked when the timer service is
     * invoked.
     */
    @Timeout
    public void timeout(Timer timer) {

        try {
            collectData();
        } catch (InternalException ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ICAT createICATLink() {
        ICAT icat = null;
        try {
            URL hostUrl;

            hostUrl = new URL(prop.getICATUrl());
            URL icatUrl = new URL(hostUrl, "ICATService/ICAT?wsdl");
            QName qName = new QName("http://icatproject.org", "ICATService");
            ICATService service = new ICATService(icatUrl, qName);
            icat = service.getICATPort();

        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return icat;
    }

    private void setupUserCollection() throws InternalException {

        List<Object> earliestUser = null;
        LocalDate earliestDashboard = getEarliestDashboard();
        try {
            earliestUser = icat.search(sessionID, "SELECT MIN(u.modTime) FROM User u");
            if (earliestUser.get(0) != null) {

                if (earliestDashboard == null) {
                    log.info("Initial Entity Collection Required.");
                    userCollector.collectUsers(dateConversion((XMLGregorianCalendar) earliestUser.get(0)), LocalDate.now());
                } 
                else if(earliestDashboard.isBefore(LocalDate.now())){
                    log.info("Top up initiated");
                    userCollector.collectUsers(earliestDashboard, LocalDate.now());
                }
            }
            else{
                log.info("No data collection required.");
            }
                    
        } catch (IcatException_Exception ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    
    
    /**
     * Get the earliest date the integrity check took place for that set collection.
     * @return The earliest date.
     */
    private LocalDate getEarliestDashboard() throws InternalException {
        List<Object> date;
        LocalDate earliest;

        date = beanManager.search("SELECT MAX(inc.checkDate) FROM IntegrityCheck inc WHERE inc.passed = 1 AND inc.collectionType=" + CollectionType.class.getName() + ".UserUpdate", manager);
        if(date.get(0)==null){
            return null;
        }
        earliest = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date.get(0)));

        return earliest;
    }

    private LocalDate dateConversion(XMLGregorianCalendar date) {
        return date.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    private void setupEntityCollection() throws InternalException {

        List<Object> earliestICAT = new ArrayList();
        List<Object> earliestDashboard = new ArrayList();

        try {
            earliestICAT = icat.search(sessionID, "SELECT MIN(d.createTime) FROM Datafile d");

            if (earliestICAT.get(0) != null) {
                earliestDashboard = beanManager.search("SELECT MIN(inc.checkDate) FROM IntegrityCheck inc WHERE inc.passed = 1 AND inc.collectionType=" + CollectionType.class.getName() + ".EntityCount", manager);

                if (earliestDashboard.get(0) == null) {
                    log.info("Initial Entity Collection Required.");
                    counter.countEntities(dateConversion((XMLGregorianCalendar) earliestICAT.get(0)), LocalDate.now());
                } else {
                    log.info("Top up initiated");
                    counter.countEntities(dateConversion((XMLGregorianCalendar) earliestDashboard.get(0)), LocalDate.now());
                }
            }

        } catch (IcatException_Exception ex) {
            java.util.logging.Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
