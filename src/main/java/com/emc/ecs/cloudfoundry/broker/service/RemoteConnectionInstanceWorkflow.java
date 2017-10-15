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
import java.util.Map;

public class RemoteConnectionInstanceWorkflow extends InstanceWorkflowImpl {

    RemoteConnectionInstanceWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException, ServiceBrokerException {
        throw new ServiceBrokerException("remote_connection parameter invalid for plan upgrade");
    }

    @Override
    public void delete(String id) throws EcsManagementClientException {
        throw new ServiceBrokerException("remote_connection parameter invalid for delete operation");
    }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan,
                                  Map<String, Object> parameters)
            throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException {
        @SuppressWarnings("unchecked")
        Map<String, String> remoteConnection = (Map<String, String>) parameters.get("remote_connection");

        // Check remote connection credentials, and that the service/plan match the original
        validateRemoteConnection(remoteConnection);

        // Fetch the service instance & update with reference
        String remoteInstanceId = remoteConnection.get("instanceId");
        ServiceInstance instance = instanceRepository.find(remoteInstanceId);
        if (instance == null)
            throw new ServiceBrokerException("Remotely connected service instance not found");

        instance.addReference(instanceId);
        instanceRepository.save(instance);

        // return this new instance to be saved
        ServiceInstance newInstance = new ServiceInstance(createRequest);
        newInstance.setName(instance.getName());
        newInstance.setReferences(instance.getReferences());
        return newInstance;
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
