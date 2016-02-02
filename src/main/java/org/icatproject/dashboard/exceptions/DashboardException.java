/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exceptions;




@SuppressWarnings("serial")
public class DashboardException extends Exception {
    private static final long serialVersionUID = 1L;

    private int httpStatusCode;
    private String message;

    public DashboardException(int httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;

        this.message = message;
    }

    public String getShortMessage() {
        return message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return "(" + httpStatusCode + ") : " + message;
    }


        
}
