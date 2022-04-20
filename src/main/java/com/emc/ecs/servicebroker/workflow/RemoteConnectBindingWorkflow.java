package com.emc.ecs.servicebroker.workflow;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.service.StorageService;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class RemoteConnectBindingWorkflow extends BindingWorkflowImpl {
    public RemoteConnectBindingWorkflow(ServiceInstanceRepository instanceRepo, StorageService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        if (instance.remoteConnectionKeyExists(bindingId))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    @Override
    public UserSecretKey createBindingUser() throws ServiceBrokerException, IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }

        String secretKey = instance.addRemoteConnectionKey(bindingId);

        instanceRepository.save(instance);

        return new UserSecretKey(secretKey);
    }

    @Override
    public Map<String, Object> getCredentials(UserSecretKey secretKey, Map<String, Object> parameters) throws IOException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put(CREDENTIALS_INSTANCE_ID, instanceId);
        credentials.put(CREDENTIALS_ACCESS_KEY, bindingId);
        credentials.put(CREDENTIALS_SECRET_KEY, secretKey.getSecretKey());
        return credentials;
    }

    @Override
    public void removeBinding()
            throws EcsManagementClientException, IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        instance.removeRemoteConnectionKey(bindingId);
        instanceRepository.save(instance);
    }

}
