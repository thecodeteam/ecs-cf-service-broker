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
import com.emc.ecs.serviceBroker.config.CatalogConfig;
import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxy;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBinding;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;

@Service
public class EcsServiceInstanceBindingService
	implements ServiceInstanceBindingService {
    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
    private static final String SERVICE_NOT_FOUND = "No service matching service id: ";
    private static final String SERVICE_TYPE = "service-type";
    private static final String NAMESPACE = "namespace";
    private static final String BUCKET = "bucket";

    @Autowired
    private EcsService ecs;

    @Autowired
    private CatalogConfig catalog;

    @Autowired
    private ServiceInstanceBindingRepository repository;

    public EcsServiceInstanceBindingService()
	    throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException, URISyntaxException {
	super();
    }

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
	    CreateServiceInstanceBindingRequest request)
	    throws ServiceInstanceBindingExistsException,
	    ServiceBrokerException {
	String instanceId = request.getServiceInstanceId();
	String bindingId = request.getBindingId();
	String serviceDefinitionId = request.getServiceDefinitionId();
	ServiceInstanceBinding binding = new ServiceInstanceBinding(request);
	Map<String, Object> credentials = new HashMap<>();
	Map<String, Object> parameters = request.getParameters();
	credentials.put("accessKey", ecs.prefix(bindingId));
	credentials.put("bucket", ecs.prefix(instanceId));
	ServiceDefinitionProxy service = catalog
		.findServiceDefinition(serviceDefinitionId);
	try {
	    if (service == null)
		throw new EcsManagementClientException(
			SERVICE_NOT_FOUND + serviceDefinitionId);

	    if (ecs.userExists(bindingId))
		throw new ServiceInstanceBindingExistsException(instanceId,
			bindingId);
	    String serviceType = (String) service.getServiceSettings()
		    .get(SERVICE_TYPE);
	    UserSecretKey userSecret;
	    URL baseUrl = new URL(ecs.getObjectEndpoint());
	    if (NAMESPACE.equals(serviceType)) {
		userSecret = ecs.createUser(bindingId, instanceId);
	    } else if (BUCKET.equals(serviceType)) {
		userSecret = ecs.createUser(bindingId);
		if (parameters != null) {
		    @SuppressWarnings("unchecked")
		    List<String> permissions = (List<String>) parameters
		    .get("permissions");
		    ecs.addUserToBucket(instanceId, bindingId, permissions);
		} else {
		    ecs.addUserToBucket(instanceId, bindingId);
		}		
	    } else {
		throw new EcsManagementClientException(
			NO_SERVICE_MATCHING_TYPE + serviceType);
	    }
	    String userInfo = bindingId + ":" + userSecret.getSecretKey();
	    String s3Url = baseUrl.getProtocol() + "://"
		    + ecs.prefix(userInfo) + "@" + baseUrl.getHost() + ":"
		    + baseUrl.getPort() + "/" + ecs.prefix(instanceId);
	    credentials.put("secretKey", userSecret.getSecretKey());
	    credentials.put("endpoint", ecs.getObjectEndpoint());
	    credentials.put("s3Url", s3Url);
	    binding.setBindingId(bindingId);
	    binding.setCredentials(credentials);
	    repository.save(binding);
	} catch (IOException | JAXBException | EcsManagementClientException e) {
	    throw new ServiceBrokerException(e);
	}
	return new CreateServiceInstanceAppBindingResponse()
		.withCredentials(credentials);
    }

    @Override
    public void deleteServiceInstanceBinding(
	    DeleteServiceInstanceBindingRequest request)
	    throws ServiceBrokerException {
	String bindingId = request.getBindingId();
	String instanceId = request.getServiceInstanceId();
	String serviceDefinitionId = request.getServiceDefinitionId();
	ServiceDefinitionProxy service = catalog
		.findServiceDefinition(serviceDefinitionId);
	String serviceType = (String) service.getServiceSettings()
		    .get(SERVICE_TYPE);
	try {
	    if (BUCKET.equals(serviceType))
		ecs.removeUserFromBucket(instanceId, bindingId);
	    ecs.deleteUser(bindingId);
	    repository.delete(bindingId);
	} catch (Exception e) {
	    throw new ServiceBrokerException(e);
	}
    }
}
