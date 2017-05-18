package com.emc.ecs.management.sdk;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.ObjectNFSAddUser;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;
import static com.emc.ecs.management.sdk.Constants.POST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUserMapAction {
    private ObjectUserMapAction() {}

    static final Logger LOG = LoggerFactory.getLogger(ObjectUserMapAction.class);

    public static void create(Connection connection, String userId, int unixUid, String namespace)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, USERS);
        connection.handleRemoteCall(POST, uri,
                new ObjectNFSAddUser(namespace, USER, userId, Integer.toString(unixUid), userId + "-umap"));
    }

    public static void delete(Connection connection, String userId, String unixUid, String namespace)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, USERS, namespace + ":id:u:" + unixUid + ":" + userId);
        LOG.info("Deleting with endpoint: " + uri);
        connection.handleRemoteCall(DELETE, uri, null);
    }
}
