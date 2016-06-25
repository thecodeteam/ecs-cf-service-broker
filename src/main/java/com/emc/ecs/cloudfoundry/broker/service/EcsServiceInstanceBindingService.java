package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EcsServiceInstanceBindingService
        implements ServiceInstanceBindingService {
    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
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
        try {
            if (ecs.userExists(bindingId))
                throw new ServiceInstanceBindingExistsException(instanceId,
                        bindingId);
            binding.setBindingId(bindingId);

            ServiceDefinitionProxy service = ecs
                    .lookupServiceDefinition(serviceDefinitionId);
            PlanProxy plan = service.findPlan(request.getPlanId());

            String serviceType = (String) service.getServiceSettings()
                    .get(SERVICE_TYPE);

            UserSecretKey userSecret;
            String s3Url;
            String endpoint;

            if (NAMESPACE.equals(serviceType)) {
                userSecret = ecs.createUser(bindingId, instanceId);
                endpoint = ecs.getNamespaceURL(ecs.prefix(instanceId), service, plan,
                        Optional.ofNullable(parameters));
                URL baseUrl = new URL(endpoint);
                String userInfo = bindingId + ":" + userSecret.getSecretKey();
                s3Url = baseUrl.getProtocol() + "://" + ecs.prefix(userInfo)
                        + "@" + baseUrl.getHost() + ":" + baseUrl.getPort();
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
                credentials.put("bucket", ecs.prefix(instanceId));
                endpoint = ecs.getObjectEndpoint();
                URL baseUrl = new URL(endpoint);
                String userInfo = bindingId + ":" + userSecret.getSecretKey();
                s3Url = baseUrl.getProtocol() + "://" + ecs.prefix(userInfo)
                        + "@" + baseUrl.getHost() + ":" + baseUrl.getPort()
                        + "/" + ecs.prefix(instanceId);
            } else {
                throw new EcsManagementClientException(
                        NO_SERVICE_MATCHING_TYPE + serviceType);
            }

            credentials.put("s3Url", s3Url);
            credentials.put("endpoint", endpoint);
            credentials.put("secretKey", userSecret.getSecretKey());
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
