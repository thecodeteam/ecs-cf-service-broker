package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class NamespaceBindingWorkflow extends BindingWorkflowImpl {

    NamespaceBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs, ServiceDefinitionProxy service) {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() {
        if (ecs.userExists(bindingId))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException {
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
        String endpoint = ecs.getNamespaceURL(ecs.prefix(namespaceName), createRequest.getParameters(), instance.getServiceSettings());
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

    private String getS3Url(String endpoint, String secretKey) throws IOException {
        URL baseUrl = new URL(endpoint);
        String encodedBinding = URLEncoder.encode(this.bindingId, "UTF-8");
        String encodedSecret = URLEncoder.encode(secretKey, "UTF-8");
        String userInfo = encodedBinding + ":" + encodedSecret;
        return baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@" + baseUrl.getHost() + ":" + baseUrl.getPort();
    }
}
