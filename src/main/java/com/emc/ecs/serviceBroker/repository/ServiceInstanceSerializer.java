package com.emc.ecs.serviceBroker.repository;

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.ServiceInstance;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class ServiceInstanceSerializer {

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
	
	@JsonIgnore
	private boolean async;
	
	@SuppressWarnings("unused")
	private ServiceInstanceSerializer() {}
	
	public ServiceInstanceSerializer(ServiceInstance instance) {
		super();
		this.serviceDefinitionId = instance.getServiceDefinitionId();
		this.planId = instance.getPlanId();
		this.organizationGuid = instance.getOrganizationGuid();
		this.spaceGuid = instance.getSpaceGuid();
		this.serviceInstanceId = instance.getServiceInstanceId();
		this.lastOperation = new LastOperationSerializer(OperationState.IN_PROGRESS, "Provisioning", false);
		
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

	public LastOperationSerializer getServiceInstanceLastOperation() {
		return lastOperation;
	}

	public ServiceInstance toServiceInstance() {
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(serviceDefinitionId, planId,
				organizationGuid, spaceGuid).withServiceInstanceId(serviceInstanceId);
		return new ServiceInstance(request);
	}
	
}
