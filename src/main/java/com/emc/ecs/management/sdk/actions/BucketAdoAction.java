package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BucketAdoUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.POST;

public class BucketAdoAction {
    private BucketAdoAction() {
    }

    public static void update(ManagementAPIConnection connection, String namespace, String bucket, boolean adoEnabled) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, ISSTALEALLOWED);
        connection.remoteCall(POST, uri, new BucketAdoUpdate(namespace, adoEnabled));
    }
}
