package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BucketTagsParamAdd;
import com.emc.ecs.management.sdk.model.BucketTagsParamDelete;
import com.emc.ecs.management.sdk.model.BucketTagsParamUpdate;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;

public class BucketTagsAction {

    private BucketTagsAction(){}

    public static void create(ManagementAPIConnection connection, String bucket, BucketTagsParamAdd tagsParam) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, TAGS);
        connection.remoteCall(POST, uri, tagsParam);
    }

    public static void update(ManagementAPIConnection connection, String bucket, BucketTagsParamUpdate tagsParam) {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, TAGS);
        connection.remoteCall(PUT, uri, tagsParam);
    }

    public static void delete(ManagementAPIConnection connection, String bucket, BucketTagsParamDelete tagsParam) {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BUCKET, bucket, TAGS);
        connection.remoteCall(DELETE, uri, tagsParam);
    }
}
