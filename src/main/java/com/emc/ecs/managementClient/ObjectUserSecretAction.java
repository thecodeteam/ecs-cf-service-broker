package com.emc.ecs.managementClient;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.managementClient.model.UserSecretKeyCreate;
import com.emc.ecs.managementClient.model.UserSecretKeyList;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class ObjectUserSecretAction {

	public static UserSecretKey create(Connection connection, String id)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment("object",
				"user-secret-keys", id);
		Response response = connection.handleRemoteCall("post", uri,
				new UserSecretKeyCreate());
		return response.readEntity(UserSecretKey.class);
	}

	public static UserSecretKey create(Connection connection, String id,
			String key) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment("object",
				"user-secret-keys", id);
		Response response = connection.handleRemoteCall("post", uri,
				new UserSecretKeyCreate(key));
		return response.readEntity(UserSecretKey.class);
	}

	public static List<UserSecretKey> list(Connection connection, String id)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment("object",
				"user-secret-keys", id);
		Response response = connection.handleRemoteCall("get", uri, null);
		return response.readEntity(UserSecretKeyList.class).asList();
	}

}