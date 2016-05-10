package com.emc.ecs.managementClient;

import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.UserCreateParam;
import com.emc.ecs.managementClient.model.UserDeleteParam;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public final class ObjectUserAction {

	private static final String USERS = "users";
	private static final String OBJECT = "object";
	
	private ObjectUserAction() {}

	public static void create(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(OBJECT, USERS);
		connection.handleRemoteCall("post", uri,
				new UserCreateParam(id, namespace));
	}

	public static boolean exists(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment(OBJECT, USERS, id, "info")
				.queryParam("namespace", namespace);
		return connection.existenceQuery(uri, null);
	}

	public static void delete(Connection connection, String id)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(OBJECT, USERS,
				"deactivate");
		connection.handleRemoteCall("post", uri, new UserDeleteParam(id));
	}

}