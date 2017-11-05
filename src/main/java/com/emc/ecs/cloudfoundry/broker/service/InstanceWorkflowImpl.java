package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;

abstract public class InstanceWorkflowImpl implements InstanceWorkflow {
    protected final EcsService ecs;
    final ServiceInstanceRepository instanceRepository;
    String instanceId;
    CreateServiceInstanceRequest createRequest;

    InstanceWorkflowImpl(ServiceInstanceRepository instanceRepo, EcsService ecs) {
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

    ServiceInstance getServiceInstance() {
        return new ServiceInstance(createRequest);
    }

}
