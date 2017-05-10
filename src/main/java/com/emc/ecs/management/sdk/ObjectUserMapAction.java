package com.emc.ecs.management.sdk;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.ObjectNFSAddUser;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;
import static com.emc.ecs.management.sdk.Constants.POST;

public class ObjectUserMapAction {
    private ObjectUserMapAction() {}

    public static String create(Connection connection, String userId, int unixUid, String namespace)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, USERS);
        connection.handleRemoteCall(POST, uri,
                new ObjectNFSAddUser(namespace, USER, userId, Integer.toString(unixUid), userId + "-umap"));
        return userId + "-umap";
    }

    public static void delete(Connection connection, String id)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, USERS, id);
        connection.handleRemoteCall(DELETE, uri, null);
    }
}
