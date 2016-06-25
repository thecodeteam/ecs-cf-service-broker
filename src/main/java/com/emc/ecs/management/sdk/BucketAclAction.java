package com.emc.ecs.management.sdk;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.BucketAcl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public final class BucketAclAction {

    private BucketAclAction() {
    }

    public static void update(Connection connection, String id, BucketAcl acl)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, id,
                ACL);
        connection.handleRemoteCall(PUT, uri, acl);
    }

    public static BucketAcl get(Connection connection, String id,
            String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, ACL)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.handleRemoteCall(GET, uri, null);
        return response.readEntity(BucketAcl.class);
    }

}