/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.icatproject.dashboard.exceptions.DashboardException;
import org.icatproject.dashboard.utility.DateUtility;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;


//@RunWith(Arquillian.class)
public class ImportCheckTest {
    
    
   // @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackage(ImportCheck.class.getPackage())
            .addPackage(DashboardException.class.getPackage())
            .addPackage(DateUtility.class.getPackage())
            .addAsWebInfResource("web.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml");
            
    }
    
    //@PersistenceContext
    EntityManager entityManager;
    
    //@Inject
    UserTransaction userTransaction;
    
    //@Rule
    public ExpectedException thrown= ExpectedException.none();
    
    
    
    //@Test
    public void no_duplicate_imports_on_same_day() throws Exception {
       
            userTransaction.begin();
            entityManager.joinTransaction();
            
            
            
            ImportCheck first = new ImportCheck(new Date(),true,"investigation");
            ImportCheck second = new ImportCheck(new Date(),false,"investigation");
            
            first.preparePersist();
            second.preparePersist();
            
            entityManager.persist(first);
            entityManager.persist(second);
            thrown.expect(Exception.class);
            userTransaction.commit();
            
            entityManager.clear();
       
        
    }
    
}
    
    