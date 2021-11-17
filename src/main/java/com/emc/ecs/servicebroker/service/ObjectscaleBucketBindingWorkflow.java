package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

public class ObjectscaleBucketBindingWorkflow implements BindingWorkflow {
    @Override
    public BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request) {
        return null;
    }

    @Override
    public BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request, ServiceInstanceBinding existingBinding) {
        return null;
    }

    @Override
    public void checkIfUserExists() throws EcsManagementClientException, IOException {

    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        return null;
    }

    @Override
    public void removeBinding() throws EcsManagementClientException, IOException, JAXBException {

    }

    @Override
    public Map<String, Object> getCredentials(String secretKey, Map<String, Object> parameters) throws IOException, EcsManagementClientException {
        return null;
    }

    @Override
    public ServiceInstanceBinding getBinding(Map<String, Object> credentials) {
        return null;
    }

    @Override
    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        return null;
    }
}
