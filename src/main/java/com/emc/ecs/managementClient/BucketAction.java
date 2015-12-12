package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.ObjectBucketCreate;
import com.emc.ecs.managementClient.model.ObjectBucketInfo;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class BucketAction {
	
	public static void create(Connection connection, String id, String namespace, String replicationGroup)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment("object", "bucket");
		connection.handleRemoteCall("post", uri, new ObjectBucketCreate(id, namespace, replicationGroup));
	}
	
	public static boolean exists(Connection connection, String id, String namespace)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "bucket", id, "info")
				.queryParam("namespace", namespace);
		return connection.existenceQuery(uri, null);
	}
	
	public static ObjectBucketInfo get(Connection connection, String id, String namespace)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "bucket", id, "info")
				.queryParam("namespace", namespace);
		Response response = connection.handleRemoteCall("get", uri, null);
		ObjectBucketInfo info = response.readEntity(ObjectBucketInfo.class);
		return info;
	}
	
	public static void delete(Connection connection, String id, String namespace)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "bucket", id, "deactivate")
				.queryParam("namespace", namespace);
		connection.handleRemoteCall("post", uri, null);
	}

}