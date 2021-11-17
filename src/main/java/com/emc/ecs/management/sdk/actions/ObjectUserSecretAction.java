package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.management.sdk.model.UserSecretKeyCreate;
import com.emc.ecs.management.sdk.model.UserSecretKeyList;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECT;
import static com.emc.ecs.management.sdk.ManagementAPIConstants.USER_SECRET_KEYS;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

public final class ObjectUserSecretAction {

    private ObjectUserSecretAction() {
    }

    public static UserSecretKey create(ManagementAPIConnection connection, String id) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, USER_SECRET_KEYS, id);
        Response response = connection.remoteCall(POST, uri, new UserSecretKeyCreate());
        return response.readEntity(UserSecretKey.class);
    }

    public static UserSecretKey create(ManagementAPIConnection connection, String id, String key) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, USER_SECRET_KEYS, id);
        Response response = connection.remoteCall(POST, uri, new UserSecretKeyCreate(key));
        return response.readEntity(UserSecretKey.class);
    }

    public static List<UserSecretKey> list(ManagementAPIConnection connection, String id) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, USER_SECRET_KEYS, id);
        Response response = connection.remoteCall(GET, uri, null);
        return response.readEntity(UserSecretKeyList.class).asList();
    }

}