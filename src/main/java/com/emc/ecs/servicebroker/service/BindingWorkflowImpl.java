package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

abstract public class BindingWorkflowImpl implements BindingWorkflow {
    ServiceInstanceRepository instanceRepository;
    protected final EcsService ecs;
    protected ServiceDefinitionProxy service;
    protected PlanProxy plan;
    String instanceId;
    String bindingId;
    CreateServiceInstanceBindingRequest createRequest;
    String username;

    BindingWorkflowImpl(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        this.instanceRepository = instanceRepo;
        this.ecs = ecs;
        this.service = service;
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

    String getUserInfo(String userSecret) {
        return bindingId + ":" + userSecret;
    }

    public ServiceInstanceBinding getBinding(Map<String, Object> credentials) {
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createRequest);
        binding.setBindingId(bindingId);
        binding.setCredentials(credentials);
        return binding;
    }

    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        // TODO add bindingExisted, & endpoints
        return CreateServiceInstanceAppBindingResponse.builder()
                .async(false)
                .credentials(credentials)
                .build();
    }

    public Map<String, Object> getCredentials(String secretKey)
            throws IOException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();

        credentials.put("accessKey", ecs.prefix(getUserName()));
        credentials.put("secretKey", secretKey);

        return credentials;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    protected String getUserName() {
        if (username == null) {
            username = new ServiceInstanceBinding(createRequest).getName();
        }

        return username;
    }
}
