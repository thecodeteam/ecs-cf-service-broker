package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.ObjectBucketCreate;
import com.emc.ecs.management.sdk.model.ObjectBucketInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public final class BucketAction {

    private BucketAction() {
    }

    public static void create(Connection connection, String id,
                              String namespace, String replicationGroup)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET);
        connection.handleRemoteCall(POST, uri,
                new ObjectBucketCreate(id, namespace, replicationGroup));
    }

    public static void create(Connection connection,
                              ObjectBucketCreate createParam)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET);
        connection.handleRemoteCall(POST, uri, createParam);
    }

    public static boolean exists(Connection connection, String id,
            String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, INFO)
                .queryParam(NAMESPACE, namespace);
        return connection.existenceQuery(uri, null);
    }

    public static ObjectBucketInfo get(Connection connection, String id,
            String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, INFO)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.handleRemoteCall(GET, uri, null);
        return response.readEntity(ObjectBucketInfo.class);
    }

    public static void delete(Connection connection, String id,
            String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, DEACTIVATE)
                .queryParam(NAMESPACE, namespace);
        connection.handleRemoteCall(POST, uri, null);
    }

}