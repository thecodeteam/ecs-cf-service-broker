package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
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

import static com.emc.ecs.servicebroker.model.Constants.*;

abstract public class BindingWorkflowImpl implements BindingWorkflow {
    ServiceInstanceRepository instanceRepository;
    protected final EcsService ecs;
    protected ServiceDefinitionProxy service;
    protected PlanProxy plan;
    String instanceId;
    String bindingId;
    CreateServiceInstanceBindingRequest createRequest;
    ServiceInstanceBinding binding;

    BindingWorkflowImpl(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        this.instanceRepository = instanceRepo;
        this.ecs = ecs;
    }

    public BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request) {
        this.instanceId = request.getServiceInstanceId();
        this.bindingId = request.getBindingId();
        this.createRequest = request;
        this.binding = new ServiceInstanceBinding(createRequest);
        return (this);
    }

    public BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request, ServiceInstanceBinding existingBinding) {
        this.instanceId = request.getServiceInstanceId();
        this.bindingId = request.getBindingId();
        this.binding = existingBinding;
        return (this);
    }

    public ServiceInstanceBinding getBinding(Map<String, Object> credentials) {
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

    public Map<String, Object> getCredentials(String secretKey) throws IOException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();

        credentials.put(CREDENTIALS_ACCESS_KEY, ecs.prefix(binding.getName()));
        credentials.put(CREDENTIALS_SECRET_KEY, secretKey);

        return credentials;
    }

}
