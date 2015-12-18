package org.dashboard.tools;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.namespace.QName;
import org.dashboard.core.collector.DataCollector;
import org.dashboard.core.entity.ICATUser;
import org.dashboard.core.exceptions.DashboardException;
import org.dashboard.core.manager.EntityBeanManager;
import org.dashboard.core.manager.ICATSessionManager;
import org.dashboard.core.manager.PropsManager;
import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Startup
@Singleton
public class DownloadGenerator {

    @PersistenceContext(unitName = "dashboard")
    private EntityManager manager;    

    @EJB
    private PropsManager prop;

    @EJB
    private EntityBeanManager beanManager;

    @EJB
    private ICATSessionManager session;

    protected ICAT icat;
    protected String sessionID;

    private long downloadSize = 0;

    @Resource(lookup = "java:jboss/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:/jms/queue/test")
    private Queue queue;

    private Session sess;

    @PostConstruct
    public void init() {
        try {
            Connection connection = connectionFactory.createConnection();
            sess = connection.createSession();
            createData(10);
        } catch (JMSException ex) {
            Logger.getLogger(DownloadGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DashboardException ex) {
            Logger.getLogger(DownloadGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Sends a message to the test queue on wildfly.
     *
     * @param message to be sent.
     */
    public void sendMessage(Message message) {

        try (JMSContext context = connectionFactory.createContext()) {
            context.createProducer().send(queue, message);
        }
    }

    public void createData(long downloadAmount) throws DashboardException {

        int count = 0;

        while (count <= downloadAmount) {

            ICATUser user = manager.find(ICATUser.class, new Long(randomGen(44603, 44701)));

            JSONObject obj = createJSON(randomGen(0, 20));
            UUID randomUUID = UUID.randomUUID();

            try {
                //prepared message.
                TextMessage prepared = sess.createTextMessage();
                prepared.setText(obj.toString());
                prepared.setStringProperty("operation", "prepareData");
                prepared.setStringProperty("user", String.valueOf(user.getName()));
                prepared.setStringProperty("ipAddress", randomIPAddress());
                prepared.setStringProperty("preparedID", randomUUID.toString());
                sendMessage(prepared);
                
                //getData message.
                
                TextMessage getData = sess.createTextMessage();  
                getData.setStringProperty("operation","getData");
                getData.setStringProperty("preparedID",randomUUID.toString());
                getData.setStringProperty("startDate",randomStartDate().toString());
                getData.setStringProperty("endDate",randomEndDate().toString());
                sendMessage(getData);

              
            } catch (JMSException ex) {
                Logger.getLogger(DownloadGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            count++;
        }
    }

    public String randomIPAddress() {
        return String.valueOf(randomGen(1, 9)) + "."+String.valueOf(randomGen(1, 200)) +"."+ String.valueOf(randomGen(1, 250)+"."+String.valueOf(randomGen(1,250)));
    }

    public Date randomStartDate() {
        long offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2012-01-07 00:00:00").getTime();
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
        return rand;

    }
    
    public Date randomEndDate(){
        long offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2012-01-07 00:00:00").getTime();
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
        return rand;
    }

    public String randomMethod() {
        ArrayList<String> methods = new ArrayList();
        methods.add("https");
        methods.add("http");
        methods.add("Globus");
        int ran = randomGen(0, 2);
        return methods.get(ran);
    }

    public JSONObject createJSON(int size) {
        JSONObject obj = new JSONObject();
        JSONArray df = new JSONArray();
        JSONArray inv = new JSONArray();
        JSONArray ds = new JSONArray();

        for (int i = 0; i < size; i++) {
            df.add(randomGen(1, 1000));
            inv.add(randomGen(1, 1000));
            ds.add(randomGen(1, 1000));
        }

        obj.put("datafileIds", df);
        obj.put("datasetIds", ds);
        obj.put("investigationIds", inv);

        return obj;
    }

    public int randomGen(int min, int max) {        
        Random rand = new Random();

        int randomNum = min + rand.nextInt((max - min) + 1);

        return randomNum;
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

}
