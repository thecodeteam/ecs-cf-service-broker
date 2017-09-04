package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;

abstract public class BindingWorkflowImpl implements BindingWorkflow {
    protected final EcsService ecs;
    protected ServiceDefinitionProxy service;
    protected PlanProxy plan;
    String instanceId;
    String bindingId;
    CreateServiceInstanceBindingRequest createRequest;

    BindingWorkflowImpl(EcsService ecs) {
        this.ecs = ecs;
    }

    public BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request) {
        this.instanceId = request.getServiceInstanceId();
        this.bindingId = request.getBindingId();
        this.createRequest = request;
        return(this);
    }

    public BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request) {
        this.instanceId = request.getServiceInstanceId();
        this.bindingId = request.getBindingId();
        return(this);
    }

    public void checkIfUserExists() throws EcsManagementClientException {
        if (ecs.userExists(bindingId))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    String getUserInfo(String userSecret) {
        return bindingId + ":" + userSecret;
    }
}
