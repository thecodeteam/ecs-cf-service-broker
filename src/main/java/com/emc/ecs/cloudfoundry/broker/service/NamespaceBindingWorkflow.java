package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NamespaceBindingWorkflow extends BindingWorkflowImpl {

    NamespaceBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        if (isRemoteConnectBinding())
            return createRemoteConnectionUser(bindingId, instanceId);
        return ecs.createUser(bindingId, instanceId).getSecretKey();
    }

    @Override
    public void removeBinding(ServiceInstanceBinding binding) throws EcsManagementClientException {
        ecs.deleteUser(bindingId);
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey)
            throws MalformedURLException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();
        if (isRemoteConnectBinding()) {
            credentials.put("remoteConnectionAccessKey", bindingId);
            credentials.put("remoteConnectionSecretKey", secretKey);
        } else {
            String endpoint = ecs.getNamespaceURL(ecs.prefix(instanceId), service, plan,
                    Optional.ofNullable(createRequest.getParameters()));
            credentials.put("accessKey", ecs.prefix(bindingId));
            credentials.put("s3Url", getS3Url(endpoint, secretKey));
            credentials.put("endpoint", endpoint);
            credentials.put("secretKey", secretKey);
        }
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
