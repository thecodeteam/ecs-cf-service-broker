package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.RetentionClassCreate;
import com.emc.ecs.management.sdk.model.RetentionClassDetails;
import com.emc.ecs.management.sdk.model.RetentionClassUpdate;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public final class NamespaceRetentionAction {

    private NamespaceRetentionAction() {
    }

    public static Boolean exists(Connection connection, String namespace,
            String retentionClass) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
                NAMESPACE, namespace, RETENTION, retentionClass);
        return connection.existenceQuery(uri, null);
    }

    public static void create(Connection connection, String namespace,
                              RetentionClassCreate createParam)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
                NAMESPACE, namespace, RETENTION);
        connection.makeRemoteCall(POST, uri, createParam);
    }

    public static void delete(Connection connection, String namespace,
            String retentionClass) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
                NAMESPACE, namespace, RETENTION, retentionClass);
        connection.makeRemoteCall(DELETE, uri, null);
    }

    public static RetentionClassDetails get(Connection connection,
            String namespace, String retentionClass)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, NAMESPACES, NAMESPACE, namespace, RETENTION,
                retentionClass);
        Response response = connection.makeRemoteCall(GET, uri, null);
        return response.readEntity(RetentionClassDetails.class);
    }

    public static void update(Connection connection, String namespace,
            String retentionClass, RetentionClassUpdate retentionClassUpdate)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
                NAMESPACE, namespace, RETENTION, retentionClass);
        connection.makeRemoteCall(PUT, uri, retentionClassUpdate);
    }
}
