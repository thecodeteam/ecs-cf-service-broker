package com.emc.ecs.servicebroker.workflow;

import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.service.StorageService;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;

import java.util.Map;

abstract public class InstanceWorkflowImpl implements InstanceWorkflow {
    protected final StorageService storage;
    final ServiceInstanceRepository instanceRepository;
    String instanceId;
    CreateServiceInstanceRequest createRequest;

    InstanceWorkflowImpl(ServiceInstanceRepository instanceRepo, StorageService storage) {
        this.instanceRepository = instanceRepo;
        this.storage = storage;
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
