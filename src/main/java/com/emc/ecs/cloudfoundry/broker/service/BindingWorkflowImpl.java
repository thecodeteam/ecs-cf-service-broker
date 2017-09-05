package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

abstract public class BindingWorkflowImpl implements BindingWorkflow {
    private Boolean remoteConnectBinding;
    private ServiceInstanceRepository instanceRepository;
    protected final EcsService ecs;
    protected ServiceDefinitionProxy service;
    protected PlanProxy plan;
    String instanceId;
    String bindingId;
    CreateServiceInstanceBindingRequest createRequest;

    BindingWorkflowImpl(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        this.instanceRepository = instanceRepo;
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

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        if (isRemoteConnectBinding()) {
            ServiceInstance instance = instanceRepository.find(instanceId);
            if (instance.remoteConnectionKeyExists(bindingId))
                throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
        } else {
            if (ecs.userExists(bindingId))
                throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
        }
    }

    Boolean isRemoteConnectBinding() {
        if (remoteConnectBinding == null) {
            Map<String, Object> parameters = createRequest.getParameters();
            if (parameters == null) {
                remoteConnectBinding = false;
            } else if (parameters.containsKey("connect_remote")) {
                remoteConnectBinding = (Boolean) parameters.get("connect_remote");
            } else {
                remoteConnectBinding = false;
            }
        }
        return remoteConnectBinding;
    }

    String createRemoteConnectionUser(String bindingId, String instanceId) throws IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        String secretKey = instance.addRemoteConnectionKey(bindingId);
        instanceRepository.save(instance);
        return secretKey;
    }

    String getUserInfo(String userSecret) {
        return bindingId + ":" + userSecret;
    }
}
