package org.dashboard.core.exceptions;

import java.net.HttpURLConnection;

public class IcatException extends DashboardException{
    private static final long serialVersionUID = 1L;

    public IcatException(String message) {
        super(HttpURLConnection.HTTP_INTERNAL_ERROR, message);
    }

}
