package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.BucketAcl;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class BucketAclAction {
	public static void update(Connection connection, String id, BucketAcl acl) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "bucket", id, "acl");
		connection.handleRemoteCall("put", uri, acl);
	}
	
	public static BucketAcl get(Connection connection, String id, String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "bucket", id, "acl")
				.queryParam("namespace", namespace);
		Response response = connection.handleRemoteCall("get", uri, null);
		return response.readEntity(BucketAcl.class);
	}

}