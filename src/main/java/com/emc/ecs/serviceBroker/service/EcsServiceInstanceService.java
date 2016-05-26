package com.emc.ecs.serviceBroker.service;

import java.net.URISyntaxException;
import java.util.Map;

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
import com.emc.ecs.serviceBroker.model.PlanProxy;
import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxy;
import com.emc.ecs.serviceBroker.repository.ServiceInstance;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceRepository;

@Service
public class EcsServiceInstanceService implements ServiceInstanceService {
    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
    private static final String NAMESPACE = "namespace";
    private static final String BUCKET = "bucket";
    private static final String SERVICE_TYPE = "service-type";

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
	    throws ServiceInstanceExistsException, ServiceBrokerException {

	ServiceInstance instance = new ServiceInstance(request);
	String serviceInstanceId = request.getServiceInstanceId();
	String serviceDefinitionId = request.getServiceDefinitionId();
	String planId = request.getPlanId();
	Map<String, Object> params = request.getParameters();
	try {
	    ServiceDefinitionProxy service = ecs
		    .lookupServiceDefinition(serviceDefinitionId);
	    PlanProxy plan = ecs.lookupPlan(service, planId);
	    String serviceType = (String) service.getServiceSettings()
		    .get(SERVICE_TYPE);
	    if (BUCKET.equals(serviceType)) {
		ecs.createBucket(serviceInstanceId, service, plan);
	    } else if (NAMESPACE.equals(serviceType)) {
		createNamespaceUnlessExists(serviceInstanceId,
			serviceDefinitionId, planId, params);
	    } else {
		throw new EcsManagementClientException(
			NO_SERVICE_MATCHING_TYPE + serviceType);
	    }

	    repository.save(instance);
	    return new CreateServiceInstanceResponse();
	} catch (Exception e) {
	    throw new ServiceBrokerException(e);
	}
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(
	    DeleteServiceInstanceRequest request)
	    throws ServiceBrokerException {

	String serviceInstanceId = request.getServiceInstanceId();
	String serviceDefinitionId = request.getServiceDefinitionId();
	try {
	    ServiceDefinitionProxy service = ecs.lookupServiceDefinition(serviceDefinitionId);

	    String serviceType = (String) service.getServiceSettings()
		    .get(SERVICE_TYPE);
	    if (BUCKET.equals(serviceType)) {
		ecs.deleteBucket(serviceInstanceId);
	    } else if (NAMESPACE.equals(serviceType)) {
		ecs.deleteNamespace(serviceInstanceId);
	    } else {
		throw new EcsManagementClientException(
			NO_SERVICE_MATCHING_TYPE + serviceType);
	    }
	    repository.delete(serviceInstanceId);
	    return new DeleteServiceInstanceResponse();
	} catch (Exception e) {
	    throw new ServiceBrokerException(e);
	}
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(
	    UpdateServiceInstanceRequest request)
	    throws ServiceInstanceUpdateNotSupportedException,
	    ServiceBrokerException, ServiceInstanceDoesNotExistException {

	String serviceInstanceId = request.getServiceInstanceId();
	String serviceDefinitionId = request.getServiceDefinitionId();
	String planId = request.getPlanId();
	Map<String, Object> params = request.getParameters();
	
	try {
	    ServiceDefinitionProxy service = ecs.lookupServiceDefinition(serviceDefinitionId);
	    ServiceInstance instance = repository.find(serviceInstanceId);
	    if (instance == null)
		throw new ServiceInstanceDoesNotExistException(serviceInstanceId);
	    
	    String serviceType = (String) service.getServiceSettings()
		    .get(SERVICE_TYPE);
	    if (BUCKET.equals(serviceType)) {
		ecs.changeBucketPlan(serviceInstanceId,
			instance.getServiceDefinitionId(), planId);
	    } else if (NAMESPACE.equals(serviceType)) {
		ecs.changeNamespacePlan(serviceInstanceId,
			instance.getServiceDefinitionId(), planId, params);
	    } else {
		throw new EcsManagementClientException(
			NO_SERVICE_MATCHING_TYPE + serviceType);
	    }

	    repository.delete(serviceInstanceId);
	    instance.update(request);
	    repository.save(instance);
	    return new UpdateServiceInstanceResponse();
	} catch (Exception e) {
	    throw new ServiceBrokerException(e);
	}
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(
	    GetLastServiceOperationRequest request) {
	return new GetLastServiceOperationResponse();
    }

    private void createNamespaceUnlessExists(String serviceInstanceId,
	    String serviceDefinitionId, String planId,
	    Map<String, Object> params) throws EcsManagementClientException {
	if (ecs.namespaceExists(serviceInstanceId))
	    throw new ServiceInstanceExistsException(serviceInstanceId,
		    serviceDefinitionId);

	ecs.createNamespace(serviceInstanceId, serviceDefinitionId, planId,
		params);

	if (!ecs.namespaceExists(serviceInstanceId))
	    throw new ServiceBrokerException(
		    "Failed to create new ECS namespace: " + serviceInstanceId);
    }
}