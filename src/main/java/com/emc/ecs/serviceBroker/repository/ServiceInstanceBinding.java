package com.emc.ecs.serviceBroker.repository;

import java.util.Map;

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
	private Map<String, Object> bindResource;

	@JsonSerialize
	@JsonProperty("parameters")
	private Map<String, Object> parameters;
	
	@JsonSerialize
	@JsonProperty("credentials")
	private Map<String, Object> credentials;
	
	public ServiceInstanceBinding() {

	}

	public ServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		super();
		this.serviceDefinitionId = request.getServiceDefinitionId();
		this.planId = request.getPlanId();
		this.bindResource = request.getBindResource();
		this.parameters = request.getParameters();
	}

	public Map<String, Object> getBindResource() {
		return bindResource;
	}

	public void setBindResource(Map<String, Object> bindResource) {
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

}
