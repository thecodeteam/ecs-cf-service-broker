package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BucketQuotaDetails;
import com.emc.ecs.management.sdk.model.BucketQuotaParam;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;

public final class BucketQuotaAction {

    private BucketQuotaAction() {
    }

    public static void create(ManagementAPIConnection connection, String namespace, String bucket, int limit, int warn) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, QUOTA);
        connection.remoteCall(PUT, uri, new BucketQuotaParam(namespace, limit, warn));
    }

    public static void delete(ManagementAPIConnection connection, String namespace, String bucket) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, bucket, QUOTA)
                .queryParam(NAMESPACE, namespace);
        connection.remoteCall(DELETE, uri, null);
    }

    public static BucketQuotaDetails get(ManagementAPIConnection connection, String namespace, String bucket) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, bucket, QUOTA)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(BucketQuotaDetails.class);
    }

}