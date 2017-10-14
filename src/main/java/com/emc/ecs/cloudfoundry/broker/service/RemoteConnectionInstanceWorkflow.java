package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RemoteConnectionInstanceWorkflow extends InstanceWorkflowImpl {
    public RemoteConnectionInstanceWorkflow(ServiceInstanceRepository repository, EcsService ecs) {
        super(repository, ecs);
    }

    @Override
    public void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> maybeParameters) throws EcsManagementClientException, ServiceBrokerException {
        throw new ServiceBrokerException("remote_connection parameter invalid for plan upgrade");
    }

    @Override
    public void delete(String id) throws EcsManagementClientException {
        throw new ServiceBrokerException("remote_connection parameter invalid for delete operation");
    }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan,
                                  Optional<Map<String, Object>> maybeParameters)
            throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException {
        Map<String, Object> parameters = maybeParameters.orElse(new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, String> remoteConnection = (Map<String, String>) parameters.get("remote_connection");

        // Check remote connection credentials, and that the service/plan match the original
        validateRemoteConnection(remoteConnection);

        // Fetch the service instance & update with reference
        ServiceInstance instance = instanceRepository.find(instanceId);
        instance.addReference(instanceId);
        instanceRepository.save(instance);

        // return this new instance to be saved
        instance.updateFromRequest(createRequest);
        return instance;
    }

    private void validateRemoteConnection(Map<String, String> remoteConnection)
            throws ServiceBrokerException, IOException {
        String instanceId = remoteConnection.get("instanceId");
        ServiceInstance instance = instanceRepository.find(instanceId);

        // Ensure that local & remote service definitions are equal
        if (! instance.getServiceDefinitionId().equals(this.createRequest.getServiceDefinitionId()))
            throw new ServiceBrokerException("service definition must match between local and remote instances");

        // Ensure that local & remote plans are equal
        if (! instance.getPlanId().equals(this.createRequest.getPlanId()))
            throw new ServiceBrokerException("service definition must match between local and remote instances");

        String accessKey = remoteConnection.get("accessKey");
        String secretKey = remoteConnection.get("secretKey");

        // Ensure that provided access & secret keys are valid
        if (! instance.remoteConnectionKeyValid(accessKey, secretKey))
            throw new ServiceBrokerException("invalid accessKey / secretKey combination");
    }
}
