package com.emc.ecs.serviceBroker.service;

import java.net.URISyntaxException;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceRepository;

@Service
public class BucketServiceInstanceService implements ServiceInstanceService {
	
	@Autowired
	private EcsService ecs;
	
	@Autowired
	private ServiceInstanceRepository repository;
	
	public BucketServiceInstanceService()
			throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		super();
	}

	@Override
	public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId, String organizationGuid,
			String spaceGuid) throws ServiceInstanceExistsException, ServiceBrokerException {
		ServiceInstance instance = new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
		try {
			if (ecs.bucketExists(instance.getId())) throw new ServiceInstanceExistsException(instance);
			ecs.createBucket(instance.getId(), planId);
			
			if (ecs.getBucketInfo(instance.getId()) == null)
				throw new ServiceBrokerException("Failed to create new ECS bucket: " + instance.getId());
			
			repository.save(instance);
			return instance;
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	@Override
	public ServiceInstance deleteServiceInstance(String id, String serviceId, String planId) throws ServiceBrokerException {
		ServiceInstance instance = null;
		try {
			instance = repository.find(id);
			ecs.deleteBucket(id);
			repository.delete(id);
			return instance;			
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	@Override
	public ServiceInstance getServiceInstance(String id) {
		try {
			return repository.find(id);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public ServiceInstance updateServiceInstance(String instanceId, String planId)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException,
			ServiceInstanceDoesNotExistException {
		try {			
			ServiceInstance instance = repository.find(instanceId);
			if (instance == null) throw new ServiceInstanceDoesNotExistException(instanceId);
			ecs.changeBucketPlan(instanceId, planId);
			repository.delete(instanceId);
			ServiceInstance updatedInstance = new ServiceInstance(instanceId, 
					instance.getServiceDefinitionId(), planId, instance.getOrganizationGuid(), 
					instance.getSpaceGuid(), instance.getDashboardUrl());
			repository.save(updatedInstance);
			return updatedInstance;
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}
}
