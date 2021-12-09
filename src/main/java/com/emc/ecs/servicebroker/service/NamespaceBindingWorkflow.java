package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.emc.ecs.servicebroker.model.Constants.*;
import static com.emc.ecs.servicebroker.service.EcsServiceInstanceBindingService.isRemoteConnectedInstance;

public class NamespaceBindingWorkflow extends BindingWorkflowImpl {
    NamespaceBindingWorkflow(ServiceInstanceRepository instanceRepo, StorageService ecs, ServiceDefinitionProxy service) {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        ServiceInstance instance = getInstance();
        if (ecs.userExists(bindingId, ecs.prefix(instance.getServiceInstanceId()))) {
            throw new ServiceInstanceBindingExistsException(instance.getServiceInstanceId(), bindingId);
        }
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException {
        ServiceInstance instance = getInstance();
        UserSecretKey userSecretKey = ecs.createUser(binding.getName(), ecs.prefix(instance.getName()));
        return userSecretKey.getSecretKey();
    }

    @Override
    public void removeBinding() throws EcsManagementClientException, IOException {
        ServiceInstance instance = getInstance();
        ecs.deleteUser(binding.getName(), ecs.prefix(instance.getServiceInstanceId()));
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey, Map<String, Object> parameters) throws IOException, EcsManagementClientException {
        ServiceInstance instance = getInstance();

        String namespaceName = instance.getName();

        // Get custom endpoint for namespace
        String endpoint = ecs.getNamespaceURL(ecs.prefix(namespaceName), createRequest.getParameters(), instance.getServiceSettings());

        Map<String, Object> credentials = super.getCredentials(secretKey);

        credentials.put(NAMESPACE, ecs.prefix(namespaceName));          // Add namespace title as part of credentials
        credentials.put(ENDPOINT, endpoint);
        credentials.put(S3_URL, buildS3Url(endpoint, secretKey));       // Add s3 URL

        return credentials;
    }

    @Override
    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        // TODO add bindingExisted, & endpoints?yy
        return CreateServiceInstanceAppBindingResponse.builder()
                .credentials(credentials)
                .build();
    }

    private String buildS3Url(String endpoint, String secretKey) throws IOException {
        URL baseUrl = new URL(endpoint);
        String encodedBinding = URLEncoder.encode(binding.getName(), "UTF-8");
        String encodedSecret = URLEncoder.encode(secretKey, "UTF-8");
        String userInfo = encodedBinding + ":" + encodedSecret;
        return baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@" + baseUrl.getHost() + ":" + baseUrl.getPort();
    }
}
