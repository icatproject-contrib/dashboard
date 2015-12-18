package org.dashboard.core.exceptions;

import java.net.HttpURLConnection;

public class NotImplementedException extends DashboardException{
    private static final long serialVersionUID = 1L;

    public NotImplementedException(String message) {
        super(HttpURLConnection.HTTP_NOT_IMPLEMENTED, message);
    }

}
