package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

public interface ManagementAPIConnection {
    Response remoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException;
    Response remoteCall(String method, UriBuilder uri, Object arg, String contentType) throws EcsManagementClientException;
    Response remoteCall(String method, UriBuilder uri, Object arg, Map<String, String> headers) throws EcsManagementClientException;
    Response makeRemoteCall(String method, UriBuilder uri, Object arg, String contentType, Map<String, String> headers) throws EcsManagementClientException;
    boolean existenceQuery(UriBuilder uri, Object arg) throws EcsManagementClientException;
    boolean existenceQuery(UriBuilder uri, Object arg, Map<String, String> headers) throws EcsManagementClientException;
    UriBuilder uriBuilder();
}
