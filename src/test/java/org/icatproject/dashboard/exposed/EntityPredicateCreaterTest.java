/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.EntityCount;
import org.icatproject.dashboard.entity.GeoLocation;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.entity.InstrumentMetaData;
import org.icatproject.dashboard.entity.InvestigationMetaData;
import static org.icatproject.dashboard.exposed.PredicateCreater.getEntityCountPredicate;
import static org.icatproject.dashboard.utility.DateUtility.convertToDate;
import static org.icatproject.dashboard.utility.DateUtility.convertToLocalDate;
import static org.icatproject.dashboard.utility.RestUtility.createPrePopulatedMap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EntityPredicateCreaterTest {
    
    
    
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(InvestigationMetaData.class.getPackage())
                .addPackage(InstrumentMetaData.class.getPackage())
                .addPackage(EntityCount.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    EntityManager manager;

    @Inject
    public UserTransaction userTransaction;
   
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
        
        LocalDate today = LocalDate.now();
        
        EntityCount count1 = new EntityCount(convertToDate(today),"Instrument",new Long(10000));
        EntityCount count2 = new EntityCount(convertToDate(today.minusDays(10)),"Dataset",new Long(100));
        EntityCount count3 = new EntityCount(convertToDate(today.minusDays(2)),"Instrument",new Long(23));
        EntityCount count4 = new EntityCount(convertToDate(today.minusDays(1)),"Datafile",new Long(10000));
        
        count1.preparePersist();
        count2.preparePersist();
        count3.preparePersist();
        count4.preparePersist();
        
        InstrumentMetaData ins1 = new InstrumentMetaData(new Long(10),new Long(10000),convertToDate(today),new Long(1));
        InstrumentMetaData ins2 = new InstrumentMetaData(new Long(20),new Long(10000),convertToDate(today.minusDays(10)),new Long(3));
        InstrumentMetaData ins3 = new InstrumentMetaData(new Long(30),new Long(30000),convertToDate(today),new Long(2));
        InstrumentMetaData ins4 = new InstrumentMetaData(new Long(40),new Long(50000),convertToDate(today.minusDays(3)),new Long(1));
        
        ins1.preparePersist();
        ins2.preparePersist();
        ins3.preparePersist();
        ins4.preparePersist();
        
        InvestigationMetaData inv1 = new InvestigationMetaData(new Long(5),new Long(10000),convertToDate(today),1);
        InvestigationMetaData inv2 = new InvestigationMetaData(new Long(4),new Long(10000),convertToDate(today),2);
        InvestigationMetaData inv3 = new InvestigationMetaData(new Long(6),new Long(10000),convertToDate(today.minusDays(1)),3);
        InvestigationMetaData inv4 = new InvestigationMetaData(new Long(10),new Long(10000),convertToDate(today.minusDays(10)),1);
        
        inv1.preparePersist();
        inv2.preparePersist();
        inv3.preparePersist();
        inv4.preparePersist();
        
        manager.persist(count1);
        manager.persist(count2);
        manager.persist(count3);
        manager.persist(count4);
        
        manager.persist(ins1);
        manager.persist(ins2);
        manager.persist(ins3);
        manager.persist(ins4);
        
        manager.persist(inv1);
        manager.persist(inv2);
        manager.persist(inv3);
        manager.persist(inv4);

        

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

        manager.createQuery("DELETE FROM InvestigationMetaData").executeUpdate();
        manager.createQuery("DELETE FROM EntityCount").executeUpdate();
        manager.createQuery("DELETE FROM InstrumentMetaData").executeUpdate();

        userTransaction.commit();

    }
    
   
    @Test
    public void count_investigations_entity(){
        
        LocalDate today = LocalDate.now();
        LocalDate before = today.minusDays(20); 
      
        
        //Criteria objects.
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<EntityCount> entityCount = query.from(EntityCount.class);         
        
        Predicate finalPredicate = getEntityCountPredicate(cb, entityCount, convertToDate(today),convertToDate(before),"Instrument");
        
        query.multiselect(entityCount.<Date>get("countDate"), entityCount.<Long>get("entityCount"));        
        
        query.where(finalPredicate); 
       
        
        List<Object[]> result = manager.createQuery(query).getResultList();
        
        long total = 0;
        
        for(Object[] temp: result){
            total += (Long)temp[1];
        }
        
        long value = 10023;
        
        assertEquals(value,total);    
        
        
    }
    
}
