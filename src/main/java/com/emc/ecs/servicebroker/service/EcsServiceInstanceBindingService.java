package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.REMOTE_CONNECTION;
import static java.lang.String.format;

@Service
public class EcsServiceInstanceBindingService implements ServiceInstanceBindingService {
    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    @Autowired
    private EcsService ecs;

    @Autowired
    private ServiceInstanceBindingRepository repository;

    @Autowired
    private ServiceInstanceRepository instanceRepo;

    public EcsServiceInstanceBindingService() {
        super();
    }

    @Override
    public Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) throws ServiceBrokerException {
        LOG.info("Creating binding '{}' for service '{}'", request.getBindingId(), request.getServiceInstanceId());
        try {
            BindingWorkflow workflow = getWorkflow(request);

            workflow.checkIfUserExists();

            String secretKey = workflow.createBindingUser();

            LOG.debug("Building binding response for binding {}", request.getBindingId());
            Map<String, Object> credentials = workflow.getCredentials(secretKey, request.getParameters());
            ServiceInstanceBinding binding = workflow.getBinding(credentials);

            LOG.debug("Saving binding {}", request.getBindingId());
            repository.save(binding);
            LOG.debug("Binding {} saved.", request.getBindingId());

            return Mono.just(workflow.getResponse(credentials));
        } catch (IOException | JAXBException | EcsManagementClientException e) {
            String errorMessage = format("Error creating binding '%s' for service '%s': %s", request.getBindingId(), request.getServiceInstanceId(), e.getMessage());
            LOG.error(errorMessage, e);
            throw new ServiceBrokerException(errorMessage, e);
        }
    }

    @Override
    public Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) throws ServiceBrokerException {
        String bindingId = request.getBindingId();

        LOG.info("Deleting binding {} ", bindingId);

        try {
            ServiceInstanceBinding binding = repository.find(bindingId);

            if (binding == null) {
                throw new ServiceInstanceBindingDoesNotExistException(bindingId);
            }

            LOG.debug("Binding found: {}", bindingId);

            BindingWorkflow workflow = getWorkflow(request, binding);

            workflow.removeBinding();

            LOG.debug("Deleting binding {}", bindingId);

            repository.delete(bindingId);

            return Mono.just(
                    DeleteServiceInstanceBindingResponse.builder()
                            .async(false)
                            .build()
            );
        } catch (Exception e) {
            String errorMessage = format("Error deleting binding '%s' for service '%s': %s", request.getBindingId(), request.getServiceInstanceId(), e.getMessage());
            LOG.error(errorMessage, e);
            throw new ServiceBrokerException(errorMessage, e);
        }
    }

    private BindingWorkflow getWorkflow(DeleteServiceInstanceBindingRequest deleteRequest, ServiceInstanceBinding existingBinding) throws EcsManagementClientException, IOException {
        if (isRemoteConnectBinding(deleteRequest)) {
            LOG.info("Remote-connect workflow for binding delete request");
            return new RemoteConnectBindingWorkflow(instanceRepo, ecs).withDeleteRequest(deleteRequest, existingBinding);
        }
        ServiceDefinitionProxy service = ecs.lookupServiceDefinition(deleteRequest.getServiceDefinitionId());
        return getWorkflow(service).withDeleteRequest(deleteRequest, existingBinding);
    }

    private BindingWorkflow getWorkflow(CreateServiceInstanceBindingRequest createRequest) throws EcsManagementClientException, IOException {
        if (isRemoteConnectBinding(createRequest)) {
            LOG.info("Remote-connect workflow for binding create request");
            return new RemoteConnectBindingWorkflow(instanceRepo, ecs).withCreateRequest(createRequest);
        }
        ServiceDefinitionProxy service = ecs.lookupServiceDefinition(createRequest.getServiceDefinitionId());
        return getWorkflow(service).withCreateRequest(createRequest);
    }

    private BindingWorkflow getWorkflow(ServiceDefinitionProxy service) throws IOException {
        ServiceType serviceType = ServiceType.fromSettings(service.getServiceSettings());
        switch (serviceType) {
            case NAMESPACE:
                return new NamespaceBindingWorkflow(instanceRepo, ecs, service);
            case BUCKET:
                return new BucketBindingWorkflow(instanceRepo, ecs);
            default:
                throw new ServiceBrokerException("Unsupported service type: " + serviceType);
        }
    }

    private boolean isRemoteConnectBinding(DeleteServiceInstanceBindingRequest deleteRequest) throws IOException {
        String bindingId = deleteRequest.getBindingId();
        ServiceInstanceBinding binding = repository.find(bindingId);
        if (binding == null) {
            throw new ServiceInstanceBindingDoesNotExistException(bindingId);
        }
        return isRemoteConnectBinding(binding.getParameters());
    }

    private Boolean isRemoteConnectBinding(CreateServiceInstanceBindingRequest createRequest) {
        Map<String, Object> parameters = createRequest.getParameters();
        return isRemoteConnectBinding(parameters);
    }

    private boolean isRemoteConnectBinding(Map<String, Object> parameters) {
        return (parameters != null)
                && parameters.containsKey(REMOTE_CONNECTION)
                && (Boolean) parameters.get(REMOTE_CONNECTION);
    }

}
