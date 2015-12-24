package com.emc.ecs.serviceBroker.repository;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class ServiceInstanceBindingSerializer {

	@JsonSerialize
	private String id;

	@JsonSerialize
	@JsonProperty("service_instance_id")
	private String serviceInstanceId;

	@JsonSerialize
	private Map<String, Object> credentials = new HashMap<String, Object>();

	@JsonSerialize
	@JsonProperty("syslog_drain_url")
	private String syslogDrainUrl;

	@JsonSerialize
	@JsonProperty("app_guid")
	private String appGuid;

	public ServiceInstanceBindingSerializer() {

	}

	public ServiceInstanceBindingSerializer(ServiceInstanceBinding binding) {
		super();
		this.id = binding.getId();
		this.serviceInstanceId = binding.getServiceInstanceId();
		this.credentials = binding.getCredentials();
		this.syslogDrainUrl = binding.getSyslogDrainUrl();
		this.appGuid = binding.getAppGuid();
	}

	public ServiceInstanceBinding toServiceInstanceBinding() {
		return new ServiceInstanceBinding(id, serviceInstanceId, credentials,
				syslogDrainUrl, appGuid);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public Map<String, Object> getCredentials() {
		return credentials;
	}

	public void setCredentials(Map<String, Object> credentials) {
		this.credentials = credentials;
	}

	public String getSyslogDrainUrl() {
		return syslogDrainUrl;
	}

	public void setSyslogDrainUrl(String syslogDrainUrl) {
		this.syslogDrainUrl = syslogDrainUrl;
	}

	public String getAppGuid() {
		return appGuid;
	}

	public void setAppGuid(String appGuid) {
		this.appGuid = appGuid;
	}

}
