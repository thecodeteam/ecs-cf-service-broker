package com.emc.ecs.servicebroker.repository;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.cloud.servicebroker.model.instance.OperationState;

import static org.junit.Assert.*;

public class SerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String V1 = "{\"service_instance_id\":\"service-instance-id\",\"service_id\":\"service-one-id\",\"plan_id\":\"plan-one-id\",\"organization_guid\":\"org-guid-one\",\"space_guid\":\"space-guid-one\",\"dashboard_url\":null,\"last_operation\":{\"description\":\"Provisioning\",\"operation_state\":\"IN_PROGRESS\",\"delete_operation\":false}}";
    public static final String V2 = "{\"service_instance_id\":\"service-instance-id\",\"service_id\":\"service-one-id\",\"plan_id\":\"plan-one-id\",\"organization_guid\":\"org-guid-one\",\"space_guid\":\"space-guid-one\",\"dashboard_url\":null,\"last_operation\":{\"description\":\"Provisioning\",\"operation_state\":\"in progress\",\"delete_operation\":false}}";

    @Test
    public void testDeserializeServiceInstanceV1() throws JsonProcessingException {
        tryDeserializeServiceInstanceJson(V1);
    }

    @Test
    public void testDeserializeServiceInstanceV2() throws JsonProcessingException {
        tryDeserializeServiceInstanceJson(V2);
    }

    public void tryDeserializeServiceInstanceJson(String json) throws JsonProcessingException {
        ServiceInstance serviceInstance = objectMapper.readValue(json, ServiceInstance.class);
        assertNotNull(serviceInstance);
        assertNotNull(serviceInstance.getLastOperation());
        assertSame(serviceInstance.getLastOperation().getOperationState(), OperationState.IN_PROGRESS);
    }
}
