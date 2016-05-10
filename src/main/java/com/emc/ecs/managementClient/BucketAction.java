package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.ObjectBucketCreate;
import com.emc.ecs.managementClient.model.ObjectBucketInfo;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public final class BucketAction {

	private static final String BUCKET = "bucket";
	private static final String OBJECT = "object";
	private static final String NAMESPACE = "namespace";

	private BucketAction() {}

	public static void create(Connection connection, String id,
			String namespace, String replicationGroup)
					throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET);
		connection.handleRemoteCall("post", uri,
				new ObjectBucketCreate(id, namespace, replicationGroup));
	}

	public static void create(Connection connection,
			ObjectBucketCreate createParam)
					throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET);
		connection.handleRemoteCall("post", uri, createParam);
	}

	public static boolean exists(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment(OBJECT, BUCKET, id, "info")
				.queryParam(NAMESPACE, namespace);
		return connection.existenceQuery(uri, null);
	}

	public static ObjectBucketInfo get(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment(OBJECT, BUCKET, id, "info")
				.queryParam(NAMESPACE, namespace);
		Response response = connection.handleRemoteCall("get", uri, null);
		ObjectBucketInfo info = response.readEntity(ObjectBucketInfo.class);
		return info;
	}

	public static void delete(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment(OBJECT, BUCKET, id, "deactivate")
				.queryParam(NAMESPACE, namespace);
		connection.handleRemoteCall("post", uri, null);
	}

}