package com.emc.ecs.serviceBroker.service;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

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
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.config.EcsConfig;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceRepository;

@Service
public class BucketServiceInstanceService implements ServiceInstanceService {
	
	private ECSService ecs;
	private ServiceInstanceRepository repo;
	
	@Autowired
	public BucketServiceInstanceService(EcsConfig config, ECSService ecs) throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		this.ecs = ecs;
		this.repo = new ServiceInstanceRepository(config, ecs);
	}

	@Override
	public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId, String organizationGuid,
			String spaceGuid) throws ServiceInstanceExistsException, ServiceBrokerException {
		ServiceInstance instance = new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
		try {
			if (ecs.bucketExists(instance.getId())) {
				ecs.deleteBucket(instance.getId());
			}
			ecs.createBucket(instance.getId(), planId);
			if (ecs.getBucketInfo(instance.getId()) == null) {
				throw new ServiceBrokerException("Failed to create new ECS bucket: " + instance.getId());
			}
			repo.save(instance);
			return instance;
		} catch (EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		} catch (EcsManagementResourceNotFoundException e) {
			throw new ServiceBrokerException(e.getMessage());
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
	public ServiceInstance deleteServiceInstance(String id, String serviceId, String planId) throws ServiceBrokerException {
		ServiceInstance instance = null;
		try {
			instance = repo.find(id);
			ecs.deleteBucket(id);
			repo.delete(id);
			return instance;			
		} catch (EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ServiceInstance getServiceInstance(String id) {
		try {
			return repo.find(id);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ServiceInstance updateServiceInstance(String instanceId, String planId)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException,
			ServiceInstanceDoesNotExistException {
		try {
			
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
		} catch (EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
