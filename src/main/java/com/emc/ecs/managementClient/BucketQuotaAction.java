package com.emc.ecs.managementClient;

import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.BucketQuotaParam;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public final class BucketQuotaAction {

	private BucketQuotaAction() {}

	public static void create(Connection connection, String id,
			String namespace, long limit, long warn)
					throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment("object", "bucket",
				id, "quota");
		connection.handleRemoteCall("put", uri,
				new BucketQuotaParam(namespace, limit, warn));
	}

	public static void delete(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "bucket", id, "quota")
				.queryParam("namespace", namespace);
		connection.handleRemoteCall("delete", uri, null);
	}

}