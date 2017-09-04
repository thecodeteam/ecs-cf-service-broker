package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;

import java.net.MalformedURLException;
import java.util.Map;

public interface BindingWorkflow {
    BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request);
    BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request);
    void checkIfUserExists() throws EcsManagementClientException;
    UserSecretKey createBindingUser() throws EcsManagementClientException;
    void removeBinding(ServiceInstanceBinding binding) throws EcsManagementClientException;
    Map<String, Object> getCredentials(String secretKey) throws MalformedURLException, EcsManagementClientException;
    ServiceInstanceBinding getBinding(Map<String, Object> credentials);
    CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials);
}
