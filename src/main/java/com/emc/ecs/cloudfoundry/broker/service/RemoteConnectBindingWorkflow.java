package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

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
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        String secretKey = instance.addRemoteConnectionKey(bindingId);
        instanceRepository.save(instance);
        return secretKey;
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
