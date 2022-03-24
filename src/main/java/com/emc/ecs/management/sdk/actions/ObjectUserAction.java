package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.UserCreateParam;
import com.emc.ecs.management.sdk.model.UserDeleteParam;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.POST;

public final class ObjectUserAction {

    private ObjectUserAction() {
    }

    public static void create(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, USERS);
        connection.remoteCall(POST, uri, new UserCreateParam(id, namespace));
    }

    public static boolean exists(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, USERS, id, INFO)
                .queryParam(NAMESPACE, namespace);
        return connection.existenceQuery(uri, null);
    }

    public static void delete(ManagementAPIConnection connection, String id) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, USERS, DEACTIVATE);
        connection.remoteCall(POST, uri, new UserDeleteParam(id));
    }

}