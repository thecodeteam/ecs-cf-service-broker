package com.emc.ecs.serviceBroker.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.serviceBroker.ECSService;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.config.EcsConfig;
import com.emc.ecs.serviceBroker.model.UserSecretKey;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;

public class BucketServiceInstanceBindingService implements ServiceInstanceBindingService {

	private ECSService ecs;
	private ServiceInstanceBindingRepository repo;
	
	@Autowired
	public BucketServiceInstanceBindingService(EcsConfig config, ECSService ecs) throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		this.ecs = ecs;
		this.repo = new ServiceInstanceBindingRepository(config, ecs);
	}

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance, String serviceId,
			String planId, String appGuid) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		String bucket = serviceId;
		String username = bindingId;		
		UserSecretKey userSecret = ecs.createUser(username);
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("accessKey", username);
		credentials.put("secretKey", userSecret.getSecretKey());
		credentials.put("bucket", serviceId);
		try {
			if (ecs.userExists(username)) ecs.deleteUser(username);
			ecs.addUserToBucket(bucket, username);
			credentials.put("endpoint", ecs.getObjectEndpoint());
			ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId, serviceId, credentials, null, appGuid);
			repo.save(binding);
			return binding;
		} catch (EcsManagementClientException | EcsManagementResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance instance, String serviceId,
			String planId) throws ServiceBrokerException {
		String username = bindingId;
		try {
			ServiceInstanceBinding binding = repo.find(username);
			ecs.deleteUser(username);			
			repo.delete(username);
			return binding;
		} catch (EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
