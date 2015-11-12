package com.emc.ecs.serviceBroker.service;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.ecs.serviceBroker.ECSService;
import com.emc.ecs.serviceBroker.ServiceInstanceRepository;

@Service
public class BucketServiceInstanceService implements ServiceInstanceService {
	
	private ECSService ecs;
	private ServiceInstanceRepository repo;
	
	@Autowired
	public BucketServiceInstanceService(ECSService ecs) {
		this.ecs = ecs;
		this.repo = new ServiceInstanceRepository(ecs);
	}

	@Override
	public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId, String organizationGuid,
			String spaceGuid) throws ServiceInstanceExistsException, ServiceBrokerException {
		ServiceInstance instance = new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
		if (ecs.bucketExists(instance.getId())) {
			ecs.deleteBucket(instance.getId());
		}
		ecs.createBucket(instance.getId(), planId);
		if (ecs.getBucketInfo(instance.getId()) == null) {
			throw new ServiceBrokerException("Failed to create new ECS bucket: " + instance.getId());
		}
		repo.save(instance);
		return instance;
	}

	@Override
	public ServiceInstance deleteServiceInstance(String id, String serviceId, String planId) throws ServiceBrokerException {
		ServiceInstance instance = repo.find(id);
		ecs.deleteBucket(id);
		repo.delete(id);
		return instance;
	}

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return repo.find(id);
	}

	@Override
	public ServiceInstance updateServiceInstance(String instanceId, String planId)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException,
			ServiceInstanceDoesNotExistException {
		ServiceInstance instance = repo.find(instanceId);
		if (instance == null) {
			throw new ServiceInstanceDoesNotExistException(instanceId);
		}
		ecs.changeBucketPlan(instanceId, planId);
		repo.delete(instanceId);
		ServiceInstance updatedInstance = new ServiceInstance(instanceId, 
				instance.getServiceDefinitionId(), planId, instance.getOrganizationGuid(), 
				instance.getSpaceGuid(), instance.getDashboardUrl());
		repo.save(updatedInstance);
		return updatedInstance;
	}
}
