package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BucketAcl;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;

public final class BucketAclAction {

    private BucketAclAction() {
    }

    public static void update(ManagementAPIConnection connection, String id, BucketAcl acl) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, id, ACL);
        connection.remoteCall(PUT, uri, acl);
    }

    public static BucketAcl get(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, ACL)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(BucketAcl.class);
    }

    public static boolean exists(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, ACL)
                .queryParam(NAMESPACE, namespace);
        return connection.existenceQuery(uri, null);
    }
}
