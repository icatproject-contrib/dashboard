package org.icatproject.dashboard.exceptions;

import java.net.HttpURLConnection;

public class ForbiddenException extends DashboardException{
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(HttpURLConnection.HTTP_FORBIDDEN, message);
    }

}
