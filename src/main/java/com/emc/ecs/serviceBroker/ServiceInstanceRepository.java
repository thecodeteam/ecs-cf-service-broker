package com.emc.ecs.serviceBroker;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;

public class ServiceInstanceRepository {
	
	private ECSService ecs;

	public ServiceInstanceRepository(ECSService ecs) {
		this.ecs = ecs;
	}

	public void save(ServiceInstance instance) {
	}

	public ServiceInstance find(String id) {
		return null;
	}

	public void delete(String id) {
	}

}
