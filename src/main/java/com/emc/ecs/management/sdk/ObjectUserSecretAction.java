package com.emc.ecs.management.sdk;

import static com.emc.ecs.management.sdk.Constants.*;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.management.sdk.model.UserSecretKeyCreate;
import com.emc.ecs.management.sdk.model.UserSecretKeyList;

public final class ObjectUserSecretAction {

    private ObjectUserSecretAction() {
    }

    public static UserSecretKey create(Connection connection, String id)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		USER_SECRET_KEYS, id);
	Response response = connection.handleRemoteCall(POST, uri,
		new UserSecretKeyCreate());
	return response.readEntity(UserSecretKey.class);
    }

    public static UserSecretKey create(Connection connection, String id,
	    String key) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		USER_SECRET_KEYS, id);
	Response response = connection.handleRemoteCall(POST, uri,
		new UserSecretKeyCreate(key));
	return response.readEntity(UserSecretKey.class);
    }

    public static List<UserSecretKey> list(Connection connection, String id)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		USER_SECRET_KEYS, id);
	Response response = connection.handleRemoteCall(GET, uri, null);
	return response.readEntity(UserSecretKeyList.class).asList();
    }

}