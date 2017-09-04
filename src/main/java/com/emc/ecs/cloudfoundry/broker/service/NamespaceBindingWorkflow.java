package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NamespaceBindingWorkflow extends BindingWorkflowImpl {

    NamespaceBindingWorkflow(EcsService ecs) {
        super(ecs);
    }

    @Override
    public UserSecretKey createBindingUser() throws EcsManagementClientException {
        return ecs.createUser(bindingId, instanceId);
    }

    @Override
    public void removeBinding(ServiceInstanceBinding binding) throws EcsManagementClientException {
        ecs.deleteUser(bindingId);
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey)
            throws MalformedURLException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();
        String endpoint = ecs.getNamespaceURL(ecs.prefix(instanceId), service, plan,
            Optional.ofNullable(createRequest.getParameters()));
        credentials.put("accessKey", ecs.prefix(bindingId));
        credentials.put("s3Url", getS3Url(endpoint, secretKey));
        credentials.put("endpoint", endpoint);
        credentials.put("secretKey", secretKey);
        return credentials;
    }

    @Override
    public ServiceInstanceBinding getBinding(Map<String, Object> credentials) {
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createRequest);
        binding.setBindingId(bindingId);
        binding.setCredentials(credentials);
        return binding;
    }

    @Override
    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        return new CreateServiceInstanceAppBindingResponse()
                .withCredentials(credentials);
    }

    private String getS3Url(String endpoint, String secretKey) throws MalformedURLException {
        URL baseUrl = new URL(endpoint);
        String userInfo = getUserInfo(secretKey);
        return baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@" +
                baseUrl.getHost() + ":" + baseUrl.getPort();
    }
}
