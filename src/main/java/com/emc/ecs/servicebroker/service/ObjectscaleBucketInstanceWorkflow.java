package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ObjectscaleBucketInstanceWorkflow implements InstanceWorkflow {
    @Override
    public InstanceWorkflow withCreateRequest(CreateServiceInstanceRequest request) {
        return null;
    }

    @Override
    public InstanceWorkflow withDeleteRequest(DeleteServiceInstanceRequest request) {
        return null;
    }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException {
        return null;
    }

    @Override
    public Map<String, Object> changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException, ServiceBrokerException, IOException {
        return null;
    }

    @Override
    public CompletableFuture delete(String id) throws EcsManagementClientException, IOException, ServiceBrokerException {
        return null;
    }
}
