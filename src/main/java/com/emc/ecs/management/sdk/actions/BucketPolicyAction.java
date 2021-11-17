package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BucketPolicy;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.service.EcsServiceInstanceBindingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class BucketPolicyAction {

    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    private BucketPolicyAction() {
    }

    public static void update(ManagementAPIConnection connection, String id, BucketPolicy policy, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        connection.remoteCall(PUT, uri, policy, APPLICATION_JSON);
    }

    /*
    * This function doesn't work - readEntity cannot read returned JSON
    * because of arrays in the returned JSON.
    * Perhaps because of the way the BucketPolicy object is structured
     */
    public static BucketPolicy get(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null, APPLICATION_JSON);
        return response.readEntity(BucketPolicy.class);
    }

    public static boolean hasPolicy(ManagementAPIConnection connection, String id, String namespace) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.remoteCall(GET, uri, null, APPLICATION_JSON);
        return response.hasEntity();
    }
}
