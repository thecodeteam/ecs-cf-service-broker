package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.DELETE;

public class SearchMetadataAction {

    private SearchMetadataAction() {

    }

    public static void delete(ManagementAPIConnection connection, String bucket, String namespace) {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, SEARCHMETADATA).queryParam(NAMESPACE, namespace);
        connection.remoteCall(DELETE, uri, null);
    }
}
