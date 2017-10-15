package com.emc.ecs.cloudfoundry.broker.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import java.util.*;

@SuppressWarnings("unused")
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
    private Map<String, String> remoteConnectionKeys = new HashMap<>();

    @JsonSerialize
    @JsonProperty("name")
    private String name;

    @JsonSerialize
    private Set<String> references = new HashSet<>();

    @JsonIgnore
    private boolean async;

    @SuppressWarnings("unused")
    private ServiceInstance() {
        name = null;
    }

    public ServiceInstance(CreateServiceInstanceRequest request) {
        super();
        serviceDefinitionId = request.getServiceDefinitionId();
        planId = request.getPlanId();
        organizationGuid = request.getOrganizationGuid();
        spaceGuid = request.getSpaceGuid();
        serviceInstanceId = request.getServiceInstanceId();
        lastOperation = new LastOperationSerializer(
                OperationState.IN_PROGRESS, "Provisioning", false);
        
        // name is set on 1st create only, not by connecting remotely
        name = serviceInstanceId;

        // add a reference to itself, used to find remotely created instances
        // of the same actual service instance
        references.add(serviceInstanceId);
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

    public Set<String> getReferences() {
        return references;
    }

    public boolean isAsync() {
        return async;
    }

    public LastOperationSerializer getServiceInstanceLastOperation() {
        return lastOperation;
    }

    public String addRemoteConnectionKey(String bindingId) {
        String secretKey = UUID.randomUUID().toString();
        addRemoteConnectionKey(bindingId, secretKey);
        return secretKey;
    }

    public void addRemoteConnectionKey(String bindingId, String secretKey) {
        this.remoteConnectionKeys.put(bindingId, secretKey);
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

    public void update(UpdateServiceInstanceRequest request) {
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.serviceInstanceId = request.getServiceInstanceId();
    }

    public String getName() {
        return name;
    }

    public void addReference(String reference) {
        this.references.add(reference);
    }

    public void setReferences(Set<String> references) {
        this.references = references;
    }

    public int getReferenceCount() {
        return this.references.size();
    }

    public void setName(String name) {
        this.name = name;
    }
}