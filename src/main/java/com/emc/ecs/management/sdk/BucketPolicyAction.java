package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.service.EcsServiceInstanceBindingService;
import com.emc.ecs.management.sdk.model.BucketPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.Constants.*;

public class BucketPolicyAction {

    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);
    private BucketPolicyAction(){
    }

    public static void update(Connection connection, String id, BucketPolicy policy, String namespace)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        connection.handleRemoteCall(PUT, uri, policy, JSON);
    }

    /*
    * This function doesn't work - readEntity cannot read returned JSON
    * because of arrays in the returned JSON.
    * Perhaps because of the way the BucketPolicy object is structured
     */
    public static BucketPolicy get(Connection connection, String id,
                                    String namespace) throws EcsManagementClientException {
        ObjectMapper objectMapper = new ObjectMapper();
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.handleRemoteCall(GET, uri, null, JSON);

        return response.readEntity(BucketPolicy.class);
    }

    public static boolean hasPolicy(Connection connection, String id,
                                   String namespace) throws EcsManagementClientException {
        ObjectMapper objectMapper = new ObjectMapper();
        UriBuilder uri = connection.getUriBuilder()
                .segment(OBJECT, BUCKET, id, POLICY)
                .queryParam(NAMESPACE, namespace);
        Response response = connection.handleRemoteCall(GET, uri, null, JSON);

        return response.hasEntity();
    }
}
