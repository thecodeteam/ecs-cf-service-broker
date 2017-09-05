package com.emc.ecs.cloudfoundry.broker.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class ServiceInstance {

    @JsonSerialize
    @JsonProperty("service_instance_id")
    private String serviceInstanceId;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("organization_guid")
    private String organizationGuid;

    @JsonSerialize
    @JsonProperty("space_guid")
    private String spaceGuid;

    @JsonSerialize
    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    @JsonSerialize
    @JsonProperty("last_operation")
    private LastOperationSerializer lastOperation;

    @JsonSerialize
    @JsonProperty("remote_connect_keys")
    private Map<String, String> remoteConnectionKeys;

    @JsonIgnore
    private boolean async;

    @SuppressWarnings("unused")
    private ServiceInstance() {
    }

    public ServiceInstance(CreateServiceInstanceRequest request) {
        super();
        this.remoteConnectionKeys = new HashMap<>();
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.organizationGuid = request.getOrganizationGuid();
        this.spaceGuid = request.getSpaceGuid();
        this.serviceInstanceId = request.getServiceInstanceId();
        this.lastOperation = new LastOperationSerializer(
                OperationState.IN_PROGRESS, "Provisioning", false);
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public String getPlanId() {
        return planId;
    }

    public String getOrganizationGuid() {
        return organizationGuid;
    }

    public String getSpaceGuid() {
        return spaceGuid;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public boolean isAsync() {
        return async;
    }

    public String addRemoteConnectionKey(String bindingId) {
        String key = UUID.randomUUID().toString();
        this.remoteConnectionKeys.put(bindingId, key);
        return key;
    }

    public Boolean remoteConnectionKeyExists(String bindingId) {
        return remoteConnectionKeys.containsKey(bindingId);
    }

    public void removeRemoteConnectionKey(String bindingId) {
        this.remoteConnectionKeys.remove(bindingId);
    }

    public Boolean remoteConnectionKeyValid(String bindingId, String remoteConnectionKey) {
        if (! remoteConnectionKeys.containsKey(bindingId))
            return false;
        String key = remoteConnectionKeys.get(bindingId);
        return (key.equals(remoteConnectionKey));
    }

    public LastOperationSerializer getServiceInstanceLastOperation() {
        return lastOperation;
    }

    public void update(UpdateServiceInstanceRequest request) {
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.serviceInstanceId = request.getServiceInstanceId();
    }

}