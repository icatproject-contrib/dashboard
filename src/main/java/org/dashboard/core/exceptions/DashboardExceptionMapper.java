/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashboard.core.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DashboardExceptionMapper implements ExceptionMapper<DashboardException> {

    @Override
    public Response toResponse(DashboardException e) {
        ErrorMessage error = new ErrorMessage();
        error.setStatus(e.getHttpStatusCode());
        error.setCode(e.getClass().getSimpleName());
        error.setMessage(e.getShortMessage());

        return Response.status(e.getHttpStatusCode()).entity(error)
                .build();
    }
}
