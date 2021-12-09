package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;

import java.util.Map;

abstract public class InstanceWorkflowImpl implements InstanceWorkflow {
    protected final StorageService ecs;
    final ServiceInstanceRepository instanceRepository;
    String instanceId;
    CreateServiceInstanceRequest createRequest;

    InstanceWorkflowImpl(ServiceInstanceRepository instanceRepo, StorageService ecs) {
        this.instanceRepository = instanceRepo;
        this.ecs = ecs;
    }

    public InstanceWorkflow withCreateRequest(CreateServiceInstanceRequest request) {
        this.instanceId = request.getServiceInstanceId();
        this.createRequest = request;
        return(this);
    }

    public InstanceWorkflow withDeleteRequest(DeleteServiceInstanceRequest request) {
        this.instanceId = request.getServiceInstanceId();
        return(this);
    }

    ServiceInstance getServiceInstance(Map<String, Object> serviceSettings) {
        ServiceInstance instance = new ServiceInstance(createRequest);
        instance.setServiceSettings(serviceSettings);
        return instance;
    }

}
