package com.emc.ecs.managementClient;

import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.model.UserCreateParam;
import com.emc.ecs.serviceBroker.model.UserDeleteParam;

public class ObjectUserAction {

	public static void create(Connection connection, String id, String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "users");
		connection.handleRemoteCall("post", uri, new UserCreateParam(id, namespace));
	}

	public static boolean exists(Connection connection, String id, String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "users", id, "info")
				.queryParam("namespace", namespace);
		return connection.existenceQuery(uri, null);
	}
	
	public static void delete(Connection connection, String id) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "users", "deactivate");
		connection.handleRemoteCall("post", uri, new UserDeleteParam(id));
	}

}