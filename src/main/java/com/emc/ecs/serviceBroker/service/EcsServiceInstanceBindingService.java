package com.emc.ecs.serviceBroker.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingSerializer;

@Service
public class EcsServiceInstanceBindingService implements ServiceInstanceBindingService {

	@Autowired
	private EcsService ecs;

	@Autowired
	private ServiceInstanceBindingRepository repository;

	public EcsServiceInstanceBindingService()
			throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		super();
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
					throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		// TODO Add parameters for binding permissions (read-only, read-write,
		// full-control, etc.)
		UserSecretKey userSecret;
		String instanceId = request.getServiceInstanceId();
		String bindingId = request.getBindingId();
		ServiceInstanceBindingSerializer binding = new ServiceInstanceBindingSerializer(request);
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("accessKey", bindingId);
		credentials.put("bucket", instanceId);
		try {
			if (ecs.userExists(bindingId))
				throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
			userSecret = ecs.createUser(bindingId);
			ecs.addUserToBucket(instanceId, bindingId);
			credentials.put("secretKey", userSecret.getSecretKey());
			credentials.put("endpoint", ecs.getObjectEndpoint());
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		binding.setCredentials(credentials);
		try {
			repository.save(binding);
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		return new CreateServiceInstanceBindingResponse(credentials);
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		String bindingId = request.getBindingId();
		String instanceId = request.getServiceInstanceId();
		try {
			ecs.deleteUser(bindingId);
			ecs.removeUserFromBucket(instanceId, bindingId);
			repository.delete(bindingId);
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}
}
