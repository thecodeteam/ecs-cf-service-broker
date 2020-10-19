package com.emc.ecs.servicebroker.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.instance.*;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.NAME_PARAMETER;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    /**
     * remains in the model for marshalling support but test harnesses should not use
     */
    @JsonSerialize
    @JsonProperty("organization_guid")
    @Deprecated
    private String organizationGuid;

    /**
     * remains in the model for marshalling support but test harnesses should not use
     */
    @JsonSerialize
    @JsonProperty("space_guid")
    @Deprecated
    private String spaceGuid;

    @JsonSerialize
    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    /**
     * remains in the model for marshalling support but test harnesses should not use
     */
    @JsonSerialize
    @JsonProperty("last_operation")
    @Deprecated
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

    @JsonSerialize
    @JsonProperty("service_settings")
    private Map<String, Object> serviceSettings;

    @SuppressWarnings("unused")
    private ServiceInstance() {
        name = null;
    }

    public ServiceInstance(CreateServiceInstanceRequest request) {
        super();
        serviceDefinitionId = request.getServiceDefinitionId();
        planId = request.getPlanId();
        serviceInstanceId = request.getServiceInstanceId();

        // name is set on 1st create only, not by connecting remotely
        if (request.getParameters() != null && request.getParameters().containsKey(NAME_PARAMETER)) {
            name = request.getParameters().get(NAME_PARAMETER) + "-" + serviceInstanceId;
        } else {
            name = serviceInstanceId;
        }

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

    public LastOperationSerializer getLastOperation() {
        return lastOperation;
    }

    public void setLastOperation(LastOperationSerializer lastOperation) {
        this.lastOperation = lastOperation;
    }

    public boolean isAsync() {
        return async;
    }

    public Mono<GetLastServiceOperationResponse> getServiceInstanceLastOperation(GetLastServiceOperationRequest request) {
        String serviceInstanceId = request.getServiceInstanceId();
        return Mono.just(GetLastServiceOperationResponse.builder()
                .operationState(OperationState.SUCCEEDED)
                .build());
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
        if (!remoteConnectionKeys.containsKey(bindingId))
            return false;
        String key = remoteConnectionKeys.get(bindingId);
        return (key.equals(remoteConnectionKey));
    }

    public void update(UpdateServiceInstanceRequest request, Map<String, Object> serviceSettings) {
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.serviceInstanceId = request.getServiceInstanceId();
        this.serviceSettings = serviceSettings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Map<String, Object> getServiceSettings() {
        return serviceSettings;
    }

    public void setServiceSettings(Map<String, Object> serviceSettings) {
        this.serviceSettings = serviceSettings;
    }
}
