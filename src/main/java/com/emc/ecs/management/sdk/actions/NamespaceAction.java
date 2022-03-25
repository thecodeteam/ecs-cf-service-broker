package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.NamespaceCreate;
import com.emc.ecs.management.sdk.model.NamespaceInfo;
import com.emc.ecs.management.sdk.model.NamespaceList;
import com.emc.ecs.management.sdk.model.NamespaceUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;

public final class NamespaceAction {

    private NamespaceAction() {
    }

    public static boolean exists(ManagementAPIConnection connection, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace);
        return connection.existenceQuery(uri, null);
    }

    public static void create(ManagementAPIConnection connection, String namespace, String namespaceAdmins, String replicationGroup) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE);
        connection.remoteCall(POST, uri, new NamespaceCreate(namespace, namespaceAdmins, replicationGroup));
    }

    public static void create(ManagementAPIConnection connection, NamespaceCreate createParam) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE);
        connection.remoteCall(POST, uri, createParam);
    }

    public static void delete(ManagementAPIConnection connection, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace, DEACTIVATE);
        connection.remoteCall(POST, uri, null);
    }

    public static NamespaceInfo get(ManagementAPIConnection connection, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(NamespaceInfo.class);
    }

    public static void update(ManagementAPIConnection connection, String namespace, NamespaceUpdate updateParam) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES, NAMESPACE, namespace);
        connection.remoteCall(PUT, uri, updateParam);
    }

    public static NamespaceList list(ManagementAPIConnection connection) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NAMESPACES);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(NamespaceList.class);
    }
}