package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.DefaultBucketRetention;
import com.emc.ecs.management.sdk.model.DefaultBucketRetentionUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;

public class BucketRetentionAction {

    private BucketRetentionAction() {
    }

    public static DefaultBucketRetention get(ManagementAPIConnection connection, String namespace, String bucket) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, bucket, RETENTION)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(DefaultBucketRetention.class);
    }

    public static void update(ManagementAPIConnection connection, String namespace, String bucket, int period) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, RETENTION);
        connection.remoteCall(PUT, uri, new DefaultBucketRetentionUpdate(namespace, period));
    }
}
