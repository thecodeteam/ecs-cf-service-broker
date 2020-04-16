package com.emc.ecs.servicebroker.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.binding.BindResource;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class ServiceInstanceBinding {

    @JsonSerialize
    @JsonProperty("binding_id")
    private String bindingId;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("bind_resource")
    private BindResource bindResource;

    @JsonSerialize
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    @JsonSerialize
    @JsonProperty("credentials")
    private Map<String, Object> credentials;

    @JsonSerialize
    @JsonProperty("volume_mounts")
    private List<VolumeMount> volumeMounts;

    public ServiceInstanceBinding() {
        super();
    }

    public ServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        super();
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.bindResource = request.getBindResource();
        this.parameters = request.getParameters();
    }

    public BindResource getBindResource() {
        return bindResource;
    }

    public void setBindResource(BindResource bindResource) {
        this.bindResource = bindResource;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public void setServiceDefinitionId(String serviceDefinitionId) {
        this.serviceDefinitionId = serviceDefinitionId;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Object> credentials) {
        this.credentials = credentials;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public List<VolumeMount> getVolumeMounts() { return volumeMounts; }

    public void setVolumeMounts(List<VolumeMount> volumeMounts) { this.volumeMounts = volumeMounts; }
}
