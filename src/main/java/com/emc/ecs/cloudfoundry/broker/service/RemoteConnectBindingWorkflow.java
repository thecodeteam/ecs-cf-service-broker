package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

public class RemoteConnectBindingWorkflow extends BindingWorkflowImpl {
    RemoteConnectBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
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
    public String createBindingUser() throws ServiceBrokerException, IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);

        String secretKey = instance.addRemoteConnectionKey(bindingId);
        instanceRepository.save(instance);
        instance.getReferences().forEach((String i) -> {
            if (! i.equals(instanceId)) {
                try {
                    ServiceInstance ref = instanceRepository.find(i);
                    ref.addRemoteConnectionKey(bindingId, secretKey);
                    instanceRepository.save(ref);
                } catch (Exception e) {
                    throw new ServiceBrokerException(e);
                }
            }
        });
        return secretKey;
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey) throws IOException, EcsManagementClientException {
        Map<String, Object> credentials = super.getCredentials(secretKey);
        credentials.put("instanceId", instanceId);
        return credentials;
    }

    @Override
    public void removeBinding(ServiceInstanceBinding binding)
            throws EcsManagementClientException, IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        instance.removeRemoteConnectionKey(bindingId);
        instanceRepository.save(instance);
    }

}
