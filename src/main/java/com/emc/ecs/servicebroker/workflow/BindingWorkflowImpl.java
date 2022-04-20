package com.emc.ecs.servicebroker.workflow;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.emc.ecs.servicebroker.model.Constants.*;
import static com.emc.ecs.servicebroker.service.EcsServiceInstanceBindingService.isRemoteConnectedInstance;

abstract public class BindingWorkflowImpl implements BindingWorkflow {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    ServiceInstanceRepository instanceRepository;
    protected final StorageService ecs;
    protected ServiceDefinitionProxy service;
    protected PlanProxy plan;
    String instanceId;
    String bindingId;
    CreateServiceInstanceBindingRequest createRequest;
    ServiceInstanceBinding binding;

    BindingWorkflowImpl(ServiceInstanceRepository instanceRepo, StorageService ecs) {
        this.instanceRepository = instanceRepo;
        this.ecs = ecs;
    }

    public BindingWorkflow withCreateRequest(CreateServiceInstanceBindingRequest request) {
        this.instanceId = request.getServiceInstanceId();
        this.bindingId = request.getBindingId();
        this.createRequest = request;
        this.binding = new ServiceInstanceBinding(createRequest);
        return (this);
    }

    public BindingWorkflow withDeleteRequest(DeleteServiceInstanceBindingRequest request, ServiceInstanceBinding existingBinding) {
        this.instanceId = request.getServiceInstanceId();
        this.bindingId = request.getBindingId();
        this.binding = existingBinding;
        return (this);
    }

    public ServiceInstanceBinding getBinding(Map<String, Object> credentials) {
        binding.setCredentials(credentials);
        return binding;
    }

    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        // TODO add bindingExisted, & endpoints
        return CreateServiceInstanceAppBindingResponse.builder()
                .async(false)
                .credentials(credentials)
                .build();
    }

    public Map<String, Object> getCredentials(UserSecretKey secretKey) throws IOException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();

        if (secretKey != null) {
            String accessKey = secretKey.getAccessKey() != null
                    ? secretKey.getAccessKey()
                    : ecs.prefix(binding.getName());

            credentials.put(CREDENTIALS_ACCESS_KEY, accessKey);
            credentials.put(CREDENTIALS_SECRET_KEY, secretKey.getSecretKey());
        }

        return credentials;
    }

    ServiceInstance getInstance() throws IOException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);

        if (instance.getName() == null)
            instance.setName(instance.getServiceInstanceId());

        if (isRemoteConnectedInstance(instance)) {
            // get remote instance ID from reference set
            LOG.info("Instance {} is remote connected, loading remote instance..", instanceId);
            Set<String> references = instance.getReferences();
            String remoteInstanceName = instance.getName();

            Optional<String> remoteId = references.stream().filter(remoteInstanceName::contains).findFirst();
            if (remoteId.isPresent()) {
                LOG.debug("Found remote instance id: {}", remoteId.get());

                instance = instanceRepository.find(remoteId.get());

                if (instance == null)
                    throw new ServiceInstanceDoesNotExistException(remoteId.get());

                if (instance.getName() == null) {
                    instance.setName(instance.getServiceInstanceId());
                }

                LOG.info("Loaded remote instance with id {}", remoteId.get());
            } else {
                throw new ServiceBrokerException("Cannot restore remote id for instance " + instanceId);
            }
        }

        Map<String, Object> serviceSettings = instance.getServiceSettings();
        if (serviceSettings == null) {
            LOG.warn("Instance doesn't contain service settings: {}", instance.getServiceInstanceId());
            throw new ServiceBrokerException("Cannot find service settings for instance " + instance.getServiceInstanceId());
        }

        return instance;
    }
}
