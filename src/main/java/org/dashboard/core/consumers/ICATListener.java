/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.consumers;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


public class ICATListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
    }
    
}
