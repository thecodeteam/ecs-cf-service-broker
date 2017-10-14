package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public interface BindingWorkflow {
    BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request);
    BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request);
    void checkIfUserExists() throws EcsManagementClientException, IOException;
    String createBindingUser() throws EcsManagementClientException, IOException, JAXBException;
    void removeBinding(ServiceInstanceBinding binding) throws EcsManagementClientException, IOException, JAXBException;
    Map<String, Object> getCredentials(String secretKey) throws IOException, EcsManagementClientException;
    ServiceInstanceBinding getBinding(Map<String, Object> credentials);
    CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials);
}