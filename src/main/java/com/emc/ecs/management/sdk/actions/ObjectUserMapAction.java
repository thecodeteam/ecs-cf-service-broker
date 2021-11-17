package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.ObjectNFSAddUser;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.POST;

public class ObjectUserMapAction {
    private ObjectUserMapAction() {
    }

    static final Logger LOG = LoggerFactory.getLogger(ObjectUserMapAction.class);

    public static void create(ManagementAPIConnection connection, String userId, int unixUid, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NFS, USERS);
        connection.remoteCall(POST, uri, new ObjectNFSAddUser(namespace, USER, userId, Integer.toString(unixUid), userId + "-umap"));
    }

    public static void delete(ManagementAPIConnection connection, String userId, String unixUid, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NFS, USERS, namespace + ":id:u:" + unixUid + ":" + userId);
        connection.remoteCall(DELETE, uri, null);
    }
}
