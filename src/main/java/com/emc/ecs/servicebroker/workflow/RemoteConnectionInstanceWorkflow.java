package com.emc.ecs.servicebroker.workflow;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.service.StorageService;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class RemoteConnectionInstanceWorkflow extends InstanceWorkflowImpl {

    public RemoteConnectionInstanceWorkflow(ServiceInstanceRepository instanceRepo, StorageService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public Map<String, Object> changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException, ServiceBrokerException {
        throw new ServiceBrokerException("remote_connection parameter invalid for plan upgrade");
    }

    @Override
    public CompletableFuture delete(String id) throws EcsManagementClientException {
        throw new ServiceBrokerException("remote_connection parameter invalid for delete operation");
    }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy serviceDef, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException {
        Map<String, String> remoteConnectionParams = getRemoteConnectionParams(parameters);
        ServiceInstance remoteInstance = getRemoteInstance(remoteConnectionParams);

        validateCredentials(remoteInstance, remoteConnectionParams);
        validateSettings(remoteInstance, serviceDef, plan, parameters);

        remoteInstance.addReference(instanceId);
        instanceRepository.save(remoteInstance);

        // return this new instance to be saved
        ServiceInstance newInstance = new ServiceInstance(createRequest);
        newInstance.setName(remoteInstance.getName());
        newInstance.setReferences(remoteInstance.getReferences());
        return newInstance;
    }

    private Map<String,String> getRemoteConnectionParams(Map<String, Object> parameters) {
        @SuppressWarnings({"unchecked"})
        Map<String, String> remoteConnection =  (Map<String, String>) parameters.get(REMOTE_CONNECTION);

        if (remoteConnection == null) {
            throw new ServiceBrokerException("Missing " + REMOTE_CONNECTION + " map in request parameters");
        }

        if (!remoteConnection.containsKey(CREDENTIALS_INSTANCE_ID)) {
            throw new ServiceBrokerException("Missing " + CREDENTIALS_INSTANCE_ID + " value in " + REMOTE_CONNECTION + " map");
        }

        if (!remoteConnection.containsKey(CREDENTIALS_ACCESS_KEY)) {
            throw new ServiceBrokerException("Missing " + CREDENTIALS_ACCESS_KEY + " value in " + REMOTE_CONNECTION + " map");
        }

        if (!remoteConnection.containsKey(CREDENTIALS_SECRET_KEY)) {
            throw new ServiceBrokerException("Missing " + CREDENTIALS_SECRET_KEY + " value in " + REMOTE_CONNECTION + " map");
        }

        return remoteConnection;
    }

    private void validateSettings(ServiceInstance remoteInstance, ServiceDefinitionProxy serviceDef, PlanProxy plan, Map<String, Object> parameters) {
        Map<String, Object> settings = ecs.mergeParameters(serviceDef, plan, parameters);

        Map<String, MapDifference.ValueDifference<Object>> settingsDiff = Maps.difference(settings, remoteInstance.getServiceSettings()).entriesDiffering();
        if (!settingsDiff.isEmpty()) {
            throw new ServiceBrokerException("service definition must match between local and remote instances");
        }
    }

    private ServiceInstance getRemoteInstance(Map<String, String> remoteConnectionParams) throws IOException {
        String remoteInstanceId = remoteConnectionParams.get(CREDENTIALS_INSTANCE_ID);
        ServiceInstance remoteInstance = instanceRepository.find(remoteInstanceId);
        if (remoteInstance == null) {
            throw new ServiceBrokerException("Remotely connected service instance not found, id: " + remoteInstanceId);
        }
        return remoteInstance;
    }

    private void validateCredentials(ServiceInstance remoteInstance, Map<String, String> remoteConnectionParams) throws ServiceBrokerException, IOException {
        String accessKey = remoteConnectionParams.get(CREDENTIALS_ACCESS_KEY);
        String secretKey = remoteConnectionParams.get(CREDENTIALS_SECRET_KEY);
        if (! remoteInstance.remoteConnectionKeyValid(accessKey, secretKey))
            throw new ServiceBrokerException("invalid accessKey / secretKey combination for remote instance " + remoteConnectionParams.get(CREDENTIALS_INSTANCE_ID));
    }
}
