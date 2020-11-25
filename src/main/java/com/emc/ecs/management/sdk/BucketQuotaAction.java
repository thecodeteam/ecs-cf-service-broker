package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.BucketQuotaDetails;
import com.emc.ecs.management.sdk.model.BucketQuotaParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public final class BucketQuotaAction {

    private BucketQuotaAction() {
    }

    public static void create(Connection connection, String namespace, String bucket, int limit, int warn)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, bucket, QUOTA);
        connection.handleRemoteCall(PUT, uri, new BucketQuotaParam(namespace, limit, warn));
    }

    public static void delete(Connection connection, String namespace, String bucket) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, bucket, QUOTA)
                .queryParam(NAMESPACE, namespace);
        connection.handleRemoteCall(DELETE, uri, null);
    }

    public static BucketQuotaDetails get(Connection connection, String namespace, String bucket) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, bucket, QUOTA)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.handleRemoteCall(GET, uri, null);
        return response.readEntity(BucketQuotaDetails.class);
    }

}