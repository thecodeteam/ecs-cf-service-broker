package com.emc.ecs.management.sdk.actions.iam;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.iam.exception.IamErrorResponse;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementClientUnauthorizedException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class IAMActionUtils {
    public static Map<String, String> accountHeader(String accountId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-emc-namespace", accountId);
        return headers;
    }

    public static Response remoteCall(ManagementAPIConnection connection, String method, UriBuilder uri, Object arg, Map<String, String> headers) throws EcsManagementClientException {
        Response response = connection.makeRemoteCall(method, uri, arg, APPLICATION_XML, headers);
        try {
            handleErrorResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            Logger.getAnonymousLogger().log(Level.FINE, "info", e);
            return response;
        }
        return response;
    }

    protected static void handleErrorResponse(Response response) throws EcsManagementClientException {
        if (response.getStatus() > 399) {
            if (response.getStatus() == 404) {
                throw new EcsManagementResourceNotFoundException(response.getStatusInfo().toString());
            }

            IamErrorResponse error = response.readEntity(IamErrorResponse.class);

            if (response.getStatus() == 401) {
                throw new EcsManagementClientUnauthorizedException(error.getErrorContent().getMessage());
            } else {
                throw new EcsManagementClientException(error.getErrorContent().getMessage());
            }
        }
    }
}
