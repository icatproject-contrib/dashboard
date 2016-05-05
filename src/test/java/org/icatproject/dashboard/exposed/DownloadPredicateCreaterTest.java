/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import org.icatproject.dashboard.entity.Download;

import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATUser;
import static org.icatproject.dashboard.exposed.PredicateCreater.createDownloadLocationPredicate;
import static org.icatproject.dashboard.exposed.PredicateCreater.createDownloadPredicate;
import static org.icatproject.dashboard.utility.DateUtility.convertToDate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class DownloadPredicateCreaterTest {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(GeoLocation.class.getPackage())
                .addPackage(Download.class.getPackage())
                .addPackage(ICATUser.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    EntityManager manager;

    @Inject
    public UserTransaction userTransaction;

    @Before
    public void setupData() throws Exception {

    }

    @Before
    public void preparePersistenceTest() throws Exception {
        clearData();
        insertData();
        startTransaction();
    }

    @After
    public void commitTransactions() throws Exception {
        userTransaction.commit();
    }

    //Inserts the data before each test
    private void insertData() throws Exception {
        userTransaction.begin();
        manager.joinTransaction();

        ICATUser testUser = new ICATUser(true, new Long(1), "Bobby McBobFace", "uows/1");
        ICATUser testUser2 = new ICATUser(true,new Long(1), "Gary", "uows/2");
        
        testUser.preparePersist();
        testUser2.preparePersist();
        
        GeoLocation location1 =  new GeoLocation(51.2, -41.3, "GB", "Towny McTownFace", "Internet", "192.168.0.1");
        GeoLocation location2 =  new GeoLocation(52.2, -41.3, "GB", "Town Land", "Internet", "192.168.0.3");
        GeoLocation location3 =  new GeoLocation(54.2, -41.2, "GB", "Town upton Town", "Internet", "192.168.0.4");
        
        location1.preparePersist();
        location2.preparePersist();
        location3.preparePersist();
        

        Download download1 = new Download(location1, testUser, "", convertToDate(LocalDate.now().minusDays(11)), convertToDate(LocalDate.now()), "https", new Long(10), 10.34, 10000, "finished", 10);
        Download download2 = new Download(location2, testUser, "", convertToDate(LocalDate.now().minusDays(12)), convertToDate(LocalDate.now().minusDays(4)), "globus", new Long(10), 10.34, 10000, "finished", 10);
        Download download3 = new Download(location3, testUser, "", convertToDate(LocalDate.now().minusDays(4)), convertToDate(LocalDate.now().minusDays(3)), "scarf", new Long(10), 10.34, 10000, "finished", 10);
        Download download4 = new Download(location1, testUser2, "", convertToDate(LocalDate.now().minusDays(2)), convertToDate(LocalDate.now()), "scarf", new Long(10), 10.34, 10000, "finished", 10);
        Download download5 = new Download(location2, testUser2, "", convertToDate(LocalDate.now().minusDays(20)), convertToDate(LocalDate.now().minusDays(15)), "scarf", new Long(10), 10.34, 10000, "finished", 10);

        download1.preparePersist();
        download2.preparePersist();
        download3.preparePersist();
        download4.preparePersist();
        download5.preparePersist();

        manager.persist(testUser);
        manager.persist(testUser2);
        
        manager.persist(location1);
        manager.persist(location2);
        manager.persist(location3);
                
        manager.persist(download1);
        manager.persist(download2);
        manager.persist(download3);
        manager.persist(download4);
        manager.persist(download5);

        userTransaction.commit();
        manager.clear();
    }

    //Starts the transaction for the searching of data
    private void startTransaction() throws Exception {
        userTransaction.begin();
        manager.joinTransaction();
    }

    //Clears the data from the temporary database.
    private void clearData() throws Exception {
        userTransaction.begin();
        manager.joinTransaction();

        manager.createQuery("DELETE FROM Download").executeUpdate();
        manager.createQuery("DELETE FROM ICATUser").executeUpdate();
        manager.createQuery("DELETE FROM GeoLocation").executeUpdate();

        userTransaction.commit();

    }

    @Test
    public void download_predicate_retrieve_all_downloads() {

        CriteriaQuery<Object> cq = getDownloadCriteriaQuery("undefined", "undefined");

        List<Object> result = manager.createQuery(cq).getResultList();

        assertEquals(4, result.size());

    }

    @Test
    public void download_predicate_retrieve_all_downloads_specfied_user() {
        CriteriaQuery<Object> cq = getDownloadCriteriaQuery("uows/1", "undefined");

        List<Object> result = manager.createQuery(cq).getResultList();

        assertEquals(3, result.size());

    }

    @Test
    public void download_predicate_retrieve_https_downloads() {

        CriteriaQuery<Object> cq = getDownloadCriteriaQuery("uows/1", "https");

        List<Object> result = manager.createQuery(cq).getResultList();

        Download download = (Download) result.get(0);

        assertEquals("https", download.getMethod());

    }

    @Test
    public void download_predicate_retrieve_scarf_downloads() {

        CriteriaQuery<Object> cq = getDownloadCriteriaQuery("undefined", "scarf");

        List<Object> result = manager.createQuery(cq).getResultList();

        Download download = (Download) result.get(0);

        assertEquals("scarf", download.getMethod());

    }

    @Test
    public void download_predicate_no_downloads_for_unknown_user() {
        CriteriaQuery<Object> cq = getDownloadCriteriaQuery("uows/3", "undefined");

        List<Object> result = manager.createQuery(cq).getResultList();

        assertEquals(0, result.size());

    }
    
    @Test
    public void location_predicate_scarf_user2(){
        
        CriteriaQuery<Object> cq = getGeoLocationCriteriaQuery("uows/2", "scarf");

        List<Object> result = manager.createQuery(cq).getResultList();
        
        GeoLocation location = (GeoLocation) result.get(0);

        assertEquals("Towny McTownFace", location.getCity());
        
        
    }
    
    @Test
    public void locatation_predicate_scarf(){
        
        CriteriaQuery<Object> cq = getGeoLocationCriteriaQuery("undefined", "scarf");

        List<Object> result = manager.createQuery(cq).getResultList();        
        
        assertEquals(2,result.size());
        
        
        
    }

    //Creates the criteriaQuery object for multiple test on geoLocation
    private CriteriaQuery<Object> getGeoLocationCriteriaQuery(String userName, String method) {
        LocalDate now = LocalDate.now();
        LocalDate before = now.minusDays(10);

        Date start = convertToDate(before);
        Date end = convertToDate(now);

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<GeoLocation> geoLocation = query.from(GeoLocation.class);

        //Join between downloads and location.
        Join<Download, GeoLocation> downloadLocationJoin = geoLocation.join("downloads");

        //Get methods and count how many their are.
        query.multiselect(geoLocation);

        Predicate finalPredicate = createDownloadLocationPredicate(cb, start, end, downloadLocationJoin, userName, method);

        query.where(finalPredicate);

       
        
        return query;
    }

    //Creates the criteriaQuery object for multiple tests on downloads. 
    private CriteriaQuery<Object> getDownloadCriteriaQuery(String userName, String method) {

        LocalDate now = LocalDate.now();
        LocalDate before = now.minusDays(10);

        Date start = convertToDate(before);
        Date end = convertToDate(now);

        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = cb.createQuery(Object.class);

        Root<Download> download = cq.from(Download.class);
        Join<Download, ICATUser> userJoin = download.join("user");

        Predicate finalPredicate = createDownloadPredicate(cb, start, end, download, userJoin, userName, method);

        cq.multiselect(download);
        cq.where(finalPredicate);

        return cq;

    }

}
