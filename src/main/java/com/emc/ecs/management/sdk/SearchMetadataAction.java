package com.emc.ecs.management.sdk;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;
import static com.emc.ecs.management.sdk.Constants.DELETE;

public class SearchMetadataAction {

    private SearchMetadataAction() {

    }

    public static void delete(Connection connection, String bucket, String namespace) {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, bucket, SEARCHMETADATA).queryParam(NAMESPACE, namespace);
        connection.handleRemoteCall(DELETE, uri, null);
    }
}
