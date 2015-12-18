package org.dashboard.core.exceptions;

import java.net.HttpURLConnection;

public class NotFoundException extends DashboardException{
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(HttpURLConnection.HTTP_NOT_FOUND, message);
    }

}
