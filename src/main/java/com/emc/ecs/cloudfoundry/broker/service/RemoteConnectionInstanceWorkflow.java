package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import jersey.repackaged.com.google.common.collect.MapDifference;
import jersey.repackaged.com.google.common.collect.Maps;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RemoteConnectionInstanceWorkflow extends InstanceWorkflowImpl {

    RemoteConnectionInstanceWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public Map<String, Object> changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException, ServiceBrokerException {
        throw new ServiceBrokerException("remote_connection parameter invalid for plan upgrade");
    }

    @Override
    public void delete(String id) throws EcsManagementClientException {
        throw new ServiceBrokerException("remote_connection parameter invalid for delete operation");
    }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy serviceDef, PlanProxy plan,
                                  Map<String, Object> parameters)
            throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException {
        Map<String, String> remoteConnection = getRemoteConnection(parameters);
        ServiceInstance remoteInstance = getRemoteInstance(remoteConnection);
        validateCredentials(remoteInstance, remoteConnection);
        validateSettings(remoteInstance, serviceDef, plan, parameters);

        remoteInstance.addReference(instanceId);
        instanceRepository.save(remoteInstance);

        // return this new instance to be saved
        ServiceInstance newInstance = new ServiceInstance(createRequest);
        newInstance.setName(remoteInstance.getName());
        newInstance.setReferences(remoteInstance.getReferences());
        return newInstance;
    }

    private Map<String,String> getRemoteConnection(Map<String, Object> parameters) {
        @SuppressWarnings({"unchecked"})
        Map<String, String> remoteConnection =  (Map<String, String>) parameters.get("remote_connection");
        return remoteConnection;
    }

    private void validateSettings(ServiceInstance remoteInstance, ServiceDefinitionProxy serviceDef, PlanProxy plan,
                                  Map<String, Object> parameters) {
        Map<String, Object> settings = new HashMap<>();
        settings.putAll(parameters);
        settings.putAll(plan.getServiceSettings());
        settings.putAll(serviceDef.getServiceSettings());

        Map<String, MapDifference.ValueDifference<Object>> settingsDiff =
                Maps.difference(settings, remoteInstance.getServiceSettings()).entriesDiffering();
        if (! settingsDiff.isEmpty())
            throw new ServiceBrokerException("service definition must match between local and remote instances");

    }

    private ServiceInstance getRemoteInstance(Map<String, String> remoteConnection) throws IOException {
        String remoteInstanceId = remoteConnection.get("instanceId");
        ServiceInstance remoteInstance = instanceRepository.find(remoteInstanceId);
        if (remoteInstance == null)
            throw new ServiceBrokerException("Remotely connected service instance not found");
        return remoteInstance;
    }

    private void validateCredentials(ServiceInstance remoteInstance, Map<String, String> remoteConnection)
            throws ServiceBrokerException, IOException {
        String accessKey = remoteConnection.get("accessKey");
        String secretKey = remoteConnection.get("secretKey");
        if (! remoteInstance.remoteConnectionKeyValid(accessKey, secretKey))
            throw new ServiceBrokerException("invalid accessKey / secretKey combination");
    }
}
