package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

public interface BindingWorkflow {
    BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request);
    BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request, ServiceInstanceBinding existingBinding);
    void checkIfUserExists() throws EcsManagementClientException, IOException;
    String createBindingUser() throws EcsManagementClientException, IOException, JAXBException;
    void removeBinding() throws EcsManagementClientException, IOException, JAXBException;
    Map<String, Object> getCredentials(String secretKey, Map<String, Object> parameters)
            throws IOException, EcsManagementClientException;
    ServiceInstanceBinding getBinding(Map<String, Object> credentials);
    CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials);
}
