package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class NamespaceBindingWorkflow extends BindingWorkflowImpl {

    NamespaceBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) throws IOException {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        if (ecs.userExists(bindingId))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);

        if (instance.getName() == null)
            instance.setName(instance.getServiceInstanceId());
        String namespaceName = instance.getName();

        return ecs.createUser(bindingId, namespaceName).getSecretKey();
    }

    @Override
    public void removeBinding(ServiceInstanceBinding binding) throws EcsManagementClientException {
        ecs.deleteUser(bindingId);
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey, Map<String, Object> parameters)
            throws IOException, EcsManagementClientException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);

        if (instance.getName() == null)
            instance.setName(instance.getServiceInstanceId());
        String namespaceName = instance.getName();

        Map<String, Object> credentials = super.getCredentials(secretKey);

        // Get custom endpoint for namespace
        String endpoint = ecs.getNamespaceURL(ecs.prefix(namespaceName), service, plan,
                createRequest.getParameters());
        credentials.put("endpoint", endpoint);

        // Add s3 URL
        credentials.put("s3Url", getS3Url(endpoint, secretKey));

        return credentials;
    }

    @Override
    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        // TODO add bindingExisted, & endpoints?yy
        return CreateServiceInstanceAppBindingResponse.builder()
                .credentials(credentials)
                .build();
    }

    private String getS3Url(String endpoint, String secretKey) throws MalformedURLException {
        URL baseUrl = new URL(endpoint);
        String userInfo = getUserInfo(secretKey);
        return baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@" +
                baseUrl.getHost() + ":" + baseUrl.getPort();
    }
}
