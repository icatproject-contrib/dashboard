package org.icatproject.dashboard.exceptions;

import java.net.HttpURLConnection;

public class InternalException extends DashboardException{
    private static final long serialVersionUID = 1L;

    public InternalException(String message) {
        super(HttpURLConnection.HTTP_INTERNAL_ERROR, message);
    }

}
