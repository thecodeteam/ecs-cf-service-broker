package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.NamespaceQuotaDetails;
import com.emc.ecs.management.sdk.model.NamespaceQuotaParam;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;

public final class NamespaceQuotaAction {

    private NamespaceQuotaAction() {
    }

    public static void create(ManagementAPIConnection connection, String namespace, NamespaceQuotaParam createParam) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, QUOTA);
        connection.remoteCall(PUT, uri, createParam);
    }

    public static NamespaceQuotaDetails get(ManagementAPIConnection connection, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, QUOTA);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(NamespaceQuotaDetails.class);
    }

    public static void delete(ManagementAPIConnection connection, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, QUOTA);
        connection.remoteCall(DELETE, uri, null);
    }
}
