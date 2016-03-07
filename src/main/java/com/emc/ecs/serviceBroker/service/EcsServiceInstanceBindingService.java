package com.emc.ecs.serviceBroker.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBinding;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;

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
		String instanceId = request.getServiceInstanceId();
		String bindingId = request.getBindingId();
		ServiceInstanceBinding binding = new ServiceInstanceBinding(request);
		Map<String, Object> credentials = new HashMap<String, Object>();
		Map<String, Object> parameters = request.getParameters();
		credentials.put("accessKey", ecs.prefix(bindingId));
		credentials.put("bucket", ecs.prefix(instanceId));
		try {
			if (ecs.userExists(bindingId))
				throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
			UserSecretKey userSecret = ecs.createUser(bindingId);
			if (parameters != null) {
				@SuppressWarnings("unchecked")
				List<String> permissions = (List<String>) parameters.get("permissions");
				ecs.addUserToBucket(instanceId, bindingId, permissions);				
			} else {
				ecs.addUserToBucket(instanceId, bindingId);
			}
			URL baseUrl = new URL(ecs.getBaseUrl());
			String userInfo = bindingId + ":" + userSecret.getSecretKey();
			String s3Url = baseUrl.getProtocol() + "://" + ecs.prefix(userInfo)
					+ "@" + baseUrl.getHost() + ":" + baseUrl.getPort() + "/"
					+ ecs.prefix(instanceId);
			credentials.put("secretKey", userSecret.getSecretKey());
			credentials.put("endpoint", ecs.getObjectEndpoint());
			credentials.put("baseUrl", ecs.getBaseUrl());
			credentials.put("s3Url", s3Url);
			binding.setBindingId(bindingId);
			binding.setCredentials(credentials);
			repository.save(binding);
		} catch (IOException | JAXBException | EcsManagementResourceNotFoundException
				| EcsManagementClientException e) {
			e.printStackTrace();
			throw new ServiceBrokerException(e.getMessage());
		}
		return new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		String bindingId = request.getBindingId();
		String instanceId = request.getServiceInstanceId();
	 	try {
			ecs.removeUserFromBucket(instanceId, bindingId);
			ecs.deleteUser(bindingId);
			repository.delete(bindingId);
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}
}
