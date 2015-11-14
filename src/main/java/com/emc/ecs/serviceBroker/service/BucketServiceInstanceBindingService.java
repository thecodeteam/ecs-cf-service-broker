package com.emc.ecs.serviceBroker.service;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.serviceBroker.ECSService;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.ServiceInstanceBindingRepository;
import com.emc.ecs.serviceBroker.model.UserSecretKey;

public class BucketServiceInstanceBindingService implements ServiceInstanceBindingService {

	private ECSService ecs;
	private ServiceInstanceBindingRepository repo;
	
	@Autowired
	public BucketServiceInstanceBindingService(ECSService ecs) {
		this.ecs = ecs;
		this.repo = new ServiceInstanceBindingRepository();
	}

	
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance, String serviceId,
			String planId, String appGuid) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		String bucket = serviceId;
		String username = bindingId;
		if (ecs.userExists(username)) {
			try {
				ecs.deleteUser(username);
			} catch (EcsManagementClientException e) {
				throw new ServiceBrokerException(e.getMessage());
			}
		}
		
		UserSecretKey userSecret = ecs.createUser(username);
		try {
			ecs.addUserToBucket(bucket, username);
		} catch (EcsManagementClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("accessKey", username);
		credentials.put("secretKey", userSecret.getSecretKey());
		try {
			credentials.put("endpoint", ecs.getObjectEndpoint(bucket));
		} catch (EcsManagementClientException | EcsManagementResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId, serviceId, credentials, null, appGuid);
		repo.save(binding);
		return binding;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance instance, String serviceId,
			String planId) throws ServiceBrokerException {
//		String bucket = serviceId;
		String username = bindingId;
		ServiceInstanceBinding binding = repo.find(username);
		try {
			ecs.deleteUser(username);			
		} catch (EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		repo.delete(username);
		return binding;
	}

}
