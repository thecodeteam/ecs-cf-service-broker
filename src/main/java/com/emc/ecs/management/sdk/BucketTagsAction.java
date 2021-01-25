package com.emc.ecs.management.sdk;

import com.emc.ecs.management.sdk.model.BucketTagsParamAdd;
import com.emc.ecs.management.sdk.model.BucketTagsParamDelete;
import com.emc.ecs.management.sdk.model.BucketTagsParamUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public class BucketTagsAction {

    private BucketTagsAction(){}

    public static void create(Connection connection, String bucket, BucketTagsParamAdd tagsParam) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, bucket, TAGS);
        connection.handleRemoteCall(POST, uri, tagsParam);
    }

    public static void update(Connection connection, String bucket, BucketTagsParamUpdate tagsParam) {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, bucket, TAGS);
        connection.handleRemoteCall(PUT, uri, tagsParam);
    }

    public static void delete(Connection connection, String bucket, BucketTagsParamDelete tagsParam) {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BUCKET, bucket, TAGS);
        connection.handleRemoteCall(DELETE, uri, tagsParam);
    }
}
