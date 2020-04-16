package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.BucketQuotaDetails;
import com.emc.ecs.management.sdk.model.BucketQuotaParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public final class BucketQuotaAction {

    private BucketQuotaAction() {
    }

    public static void create(Connection connection, String id,
                              String namespace, int limit, int warn)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, id,
                QUOTA);
        connection.handleRemoteCall(PUT, uri,
                new BucketQuotaParam(namespace, limit, warn));
    }

    public static void delete(Connection connection, String id,
            String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, QUOTA)
                .queryParam(NAMESPACE, namespace);
        connection.handleRemoteCall(DELETE, uri, null);
    }

    public static BucketQuotaDetails get(Connection connection, String id,
            String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, QUOTA)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.handleRemoteCall(GET, uri, null);
        return response.readEntity(BucketQuotaDetails.class);
    }

}