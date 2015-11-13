/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.exposed;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Login {
    
    private String authenticator;
    private String username;
    private String password;

    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    

    public String getAuthenticator() {
        return authenticator;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
   
    
}
