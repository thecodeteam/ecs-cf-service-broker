package com.emc.ecs.serviceBroker;

import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;

public class ServiceInstanceBindingRepository {

	private ECSService ecs;

	public ServiceInstanceBindingRepository(ECSService ecs) {
		this.ecs = ecs;
	}

	public void save(ServiceInstanceBinding binding) {
	}

	public ServiceInstanceBinding find(String username) {
		return null;
	}

	public void delete(String username) {
	}

}
