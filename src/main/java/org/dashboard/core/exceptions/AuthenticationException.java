package org.dashboard.core.exceptions;

import java.net.HttpURLConnection;

public class AuthenticationException extends DashboardException{
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(HttpURLConnection.HTTP_UNAUTHORIZED, message);
    }

}
