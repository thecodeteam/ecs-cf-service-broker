package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class RemoteConnectionInstanceWorkflow extends InstanceWorkflowImpl {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RemoteConnectionInstanceWorkflow.class);
    private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

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
        Map<String, Object> localSettings = ecs.mergeParameters(serviceDef, plan, parameters);
        // ignore bucket tags and search metadata
        localSettings.remove(TAGS);
        localSettings.remove(SEARCH_METADATA);

        Map<String, Object> remoteSettings = remoteInstance.getServiceSettings();
        // ignore bucket tags and search metadata
        remoteSettings.remove(TAGS);
        remoteSettings.remove(SEARCH_METADATA);

        Map<String, MapDifference.ValueDifference<Object>> settingsDiff = Maps.difference(localSettings, remoteSettings).entriesDiffering();

        // remove all map entries which are UUIDs
        Map<String, MapDifference.ValueDifference<Object>> settingsDiffNoUUIDs = settingsDiff.entrySet().stream()
            .filter(entry -> !entryIsUUID(entry))
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        if (!settingsDiffNoUUIDs.isEmpty()) {
            String diffStr = diffMapToString(settingsDiffNoUUIDs);
            logger.error("validateSettings found the following settings differences: {}", diffStr);
            throw new ServiceBrokerException("service definition must match between local and remote instances");
        }
    }

    public String diffMapToString(Map<String, MapDifference.ValueDifference<Object>> map) {
        String mapAsString = map.keySet().stream()
            .map(key -> key + "=" + "(" + map.get(key).leftValue() + ", " + map.get(key).rightValue() + ")")
            .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
      }

    private boolean entryIsUUID(Map.Entry<String, MapDifference.ValueDifference<Object>> entry) {
        Object left = entry.getValue().leftValue();
        Object right = entry.getValue().rightValue();
        return stringIsUUID(entry.getKey()) || left instanceof String && stringIsUUID(left.toString()) || right instanceof String && stringIsUUID(right.toString());
    }

    private boolean stringIsUUID(String str) {
        if (str == null) {
          return false;
        }
        return UUID_REGEX.matcher(str).matches();
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
