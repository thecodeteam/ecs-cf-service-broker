package com.emc.ecs.managementClient;

import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.UserCreateParam;
import com.emc.ecs.managementClient.model.UserDeleteParam;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import static com.emc.ecs.managementClient.Constants.*;

public final class ObjectUserAction {
	
	private ObjectUserAction() {}

	public static void create(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(OBJECT, USERS);
		connection.handleRemoteCall(POST, uri,
				new UserCreateParam(id, namespace));
	}

	public static boolean exists(Connection connection, String id,
			String namespace) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment(OBJECT, USERS, id, INFO)
				.queryParam(NAMESPACE, namespace);
		return connection.existenceQuery(uri, null);
	}

	public static void delete(Connection connection, String id)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(OBJECT, USERS,
				DEACTIVATE);
		connection.handleRemoteCall(POST, uri, new UserDeleteParam(id));
	}

}