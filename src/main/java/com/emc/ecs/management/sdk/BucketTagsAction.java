package com.emc.ecs.management.sdk;

import com.emc.ecs.management.sdk.model.BucketTagsParam;
import com.emc.ecs.management.sdk.model.BucketTagsParamAdd;
import com.emc.ecs.management.sdk.model.BucketTagsParamDelete;
import com.emc.ecs.management.sdk.model.BucketTagsParamUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.Map;

import static com.emc.ecs.management.sdk.Constants.*;

public class BucketTagsAction {

    private BucketTagsAction(){}

    public static void create(Connection connection, String id, BucketTagsParamAdd tagsParam)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, id, TAGS);
        connection.handleRemoteCall(POST, uri, tagsParam);
    }

    public static void update(Connection connection, String id, BucketTagsParamUpdate tagsParam) {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, id, TAGS);
        connection.handleRemoteCall(PUT, uri, tagsParam);
    }
    public static void delete(Connection connection, String id, BucketTagsParamDelete tagsParam) {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, id, TAGS);
        connection.handleRemoteCall(DELETE, uri, tagsParam);
    }
}
