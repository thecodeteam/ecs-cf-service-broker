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

import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;

@Service
public class BucketServiceInstanceBindingService implements ServiceInstanceBindingService {

	@Autowired
	private EcsService ecs;
	
	@Autowired
	private ServiceInstanceBindingRepository repository;
	
	
	public BucketServiceInstanceBindingService()
			throws EcsManagementClientException,
			EcsManagementResourceNotFoundException, URISyntaxException {
		super();
	}

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance, String serviceId,
			String planId, String appGuid) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		// TODO Add parameters for binding permissions (read-only, read-write, full-controll, etc.)
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
			credentials.put("secretKey", userSecret.getSecretKey());
			credentials.put("endpoint", ecs.getObjectEndpoint());
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		binding = new ServiceInstanceBinding(bindingId, instanceId, credentials, null, appGuid);
		try {
			repository.save(binding);
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
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
