package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public interface ManagementAPIConnection {
    Response remoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException;
    Response remoteCall(String method, UriBuilder uri, Object arg, String contentType) throws EcsManagementClientException;
    boolean existenceQuery(UriBuilder uri, Object arg) throws EcsManagementClientException;
    UriBuilder uriBuilder();
}
