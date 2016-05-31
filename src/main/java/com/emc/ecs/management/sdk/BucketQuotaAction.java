package com.emc.ecs.management.sdk;

import static com.emc.ecs.management.sdk.Constants.*;

import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.BucketQuotaParam;

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

}