package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.RetentionClassCreate;
import com.emc.ecs.management.sdk.model.RetentionClassDetails;
import com.emc.ecs.management.sdk.model.RetentionClassUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;

public final class NamespaceRetentionAction {

    private NamespaceRetentionAction() {
    }

    public static Boolean exists(ManagementAPIConnection connection, String namespace, String retentionClass) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, RETENTION, retentionClass);
        return connection.existenceQuery(uri, null);
    }

    public static void create(ManagementAPIConnection connection, String namespace, RetentionClassCreate createParam) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, RETENTION);
        connection.remoteCall(POST, uri, createParam);
    }

    public static void delete(ManagementAPIConnection connection, String namespace, String retentionClass) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, RETENTION, retentionClass);
        connection.remoteCall(DELETE, uri, null);
    }

    public static RetentionClassDetails get(ManagementAPIConnection connection, String namespace, String retentionClass) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, RETENTION, retentionClass);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(RetentionClassDetails.class);
    }

    public static void update(ManagementAPIConnection connection, String namespace, String retentionClass, RetentionClassUpdate retentionClassUpdate) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, RETENTION, retentionClass);
        connection.remoteCall(PUT, uri, retentionClassUpdate);
    }
}
