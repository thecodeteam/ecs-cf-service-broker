package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.ObjectBucketCreate;
import com.emc.ecs.management.sdk.model.ObjectBucketInfo;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

public final class BucketAction {

    private BucketAction() {
    }

    public static void create(ManagementAPIConnection connection, String id, String namespace, String replicationGroup) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET);
        connection.remoteCall(POST, uri, new ObjectBucketCreate(id, namespace, replicationGroup));
    }

    public static void create(ManagementAPIConnection connection, ObjectBucketCreate createParam) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET);
        connection.remoteCall(POST, uri, createParam);
    }

    public static boolean exists(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, INFO)
                .queryParam(NAMESPACE, namespace);
        return connection.existenceQuery(uri, null);
    }

    public static ObjectBucketInfo get(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, INFO)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(ObjectBucketInfo.class);
    }

    public static void delete(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, DEACTIVATE)
                .queryParam(NAMESPACE, namespace);
        connection.remoteCall(POST, uri, null);
    }
}