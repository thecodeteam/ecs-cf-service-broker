package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BucketPolicy;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class BucketPolicyAction {
    private static final Logger LOG = LoggerFactory.getLogger(BucketPolicyAction.class);

    private BucketPolicyAction() {
    }

    public static void update(ManagementAPIConnection connection, String id, BucketPolicy policy, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        connection.remoteCall(PUT, uri, policy, APPLICATION_JSON);
    }

    public static BucketPolicy get(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        try (Response response = connection.remoteCall(GET, uri, null, Collections.singletonMap(HttpHeaders.ACCEPT, APPLICATION_JSON))) {
            return response.readEntity(BucketPolicy.class);
        }
    }

    public static boolean hasPolicy(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        try (Response response = connection.remoteCall(GET, uri, null, Collections.singletonMap(HttpHeaders.ACCEPT, APPLICATION_JSON))) {
            return response.hasEntity();
        }
    }

    public static void remove(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        connection.remoteCall(DELETE, uri, null);
    }
}
