package com.emc.ecs.serviceBroker.service;

import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceRepository;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceSerializer;

@Service
public class EcsServiceInstanceService implements ServiceInstanceService {

	@Autowired
	private EcsService ecs;

	@Autowired
	private ServiceInstanceRepository repository;

	public EcsServiceInstanceService() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException, URISyntaxException {
		super();
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(
			CreateServiceInstanceRequest request)
					throws ServiceInstanceExistsException,
					ServiceBrokerException {
		ServiceInstanceSerializer instance = new ServiceInstanceSerializer(request);
		String serviceInstanceId = request.getServiceInstanceId();
		String serviceDefinitionId = request.getServiceDefinitionId();
		try {
			if (ecs.bucketExists(serviceInstanceId))
				throw new ServiceInstanceExistsException(serviceInstanceId,
						serviceDefinitionId);
			ecs.createBucket(serviceInstanceId, serviceDefinitionId,
					request.getPlanId());

			if (ecs.getBucketInfo(serviceInstanceId) == null)
				throw new ServiceBrokerException(
						"Failed to create new ECS bucket: "
								+ serviceInstanceId);

			repository.save(instance);
			return new CreateServiceInstanceResponse();
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(
			DeleteServiceInstanceRequest request)
					throws ServiceBrokerException {
		String serviceInstanceId = request.getServiceInstanceId();
		try {
			ecs.deleteBucket(serviceInstanceId);
			repository.delete(serviceInstanceId);
			return new DeleteServiceInstanceResponse();
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(
			UpdateServiceInstanceRequest request)
					throws ServiceInstanceUpdateNotSupportedException,
					ServiceBrokerException,
					ServiceInstanceDoesNotExistException {
		String instanceId = request.getServiceInstanceId();
		String planId = request.getPlanId();
		try {
			ServiceInstanceSerializer instance = repository.find(instanceId);
			if (instance == null)
				throw new ServiceInstanceDoesNotExistException(instanceId);
			ecs.changeBucketPlan(instanceId, instance.getServiceDefinitionId(),
					planId);
			repository.delete(instanceId);
			instance.update(request);
			repository.save(instance);
			return new UpdateServiceInstanceResponse();
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(
			GetLastServiceOperationRequest request) {
		return new GetLastServiceOperationResponse(null);
	}
}
