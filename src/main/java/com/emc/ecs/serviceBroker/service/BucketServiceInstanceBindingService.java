package com.emc.ecs.serviceBroker.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.model.UserSecretKey;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;

@Service
public class BucketServiceInstanceBindingService implements ServiceInstanceBindingService {

	private EcsService ecs;
	private ServiceInstanceBindingRepository repository;
	
	@Autowired
	public BucketServiceInstanceBindingService(EcsService ecs) throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		this.ecs = ecs;
		this.repository = new ServiceInstanceBindingRepository(ecs);
	}

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance, String serviceId,
			String planId, String appGuid) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		UserSecretKey userSecret;
		String instanceId = serviceInstance.getId();
		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId, instanceId, null, null, appGuid);
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("accessKey", bindingId);
		credentials.put("bucket", instanceId);
		try {
			if (ecs.userExists(bindingId)) throw new ServiceInstanceBindingExistsException(binding);
			userSecret = ecs.createUser(bindingId);
			ecs.addUserToBucket(instanceId, bindingId);
			repository.save(binding);
			credentials.put("secretKey", userSecret.getSecretKey());
			credentials.put("endpoint", ecs.getObjectEndpoint());
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		binding = new ServiceInstanceBinding(bindingId, instanceId, credentials, null, appGuid);
		return binding;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance instance, String serviceId,
			String planId) throws ServiceBrokerException {
		try {
			ServiceInstanceBinding binding = repository.find(bindingId);
			ecs.deleteUser(bindingId);
			// TODO Delete User ACLs as a cleanup exercise
			repository.delete(bindingId);
			return binding;
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}
}
