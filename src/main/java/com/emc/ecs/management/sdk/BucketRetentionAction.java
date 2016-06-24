package com.emc.ecs.management.sdk;

import static com.emc.ecs.management.sdk.Constants.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.DefaultBucketRetention;
import com.emc.ecs.management.sdk.model.DefaultBucketRetentionUpdate;

public class BucketRetentionAction {

    private BucketRetentionAction() {}

    public static DefaultBucketRetention get(Connection connection,
		String namespace, String bucket) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder()
			.segment(OBJECT, BUCKET, bucket, RETENTION)
			.queryParam(NAMESPACE, namespace);
	Response response = connection.makeRemoteCall(GET, uri, null);
	return response.readEntity(DefaultBucketRetention.class);
    }

    public static void update(Connection connection, String namespace,
	    String bucket, int period)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder()
			.segment(OBJECT, BUCKET, bucket, RETENTION);
	connection.makeRemoteCall(PUT, uri, new DefaultBucketRetentionUpdate(namespace, period));
    }
}
