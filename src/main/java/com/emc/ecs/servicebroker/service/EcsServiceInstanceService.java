package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.lang.String.format;

@Service
public class EcsServiceInstanceService implements ServiceInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(EcsServiceInstanceService.class);

    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
    private static final String NAMESPACE = "namespace";
    private static final String BUCKET = "bucket";
    private static final String SERVICE_TYPE = "service-type";
    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceService.class);

    @Autowired
    private EcsService ecs;

    @Autowired
    private ServiceInstanceRepository repository;

    public EcsServiceInstanceService() {
        super();
    }

    EcsServiceInstanceService(EcsService ecs, ServiceInstanceRepository repo) {
        super();
        this.ecs = ecs;
        this.repository = repo;
    }

    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        String serviceInstanceId = request.getServiceInstanceId();
        String serviceDefinitionId = request.getServiceDefinitionId();
        String planId = request.getPlanId();

        logger.info(format("Creating service instance %s", serviceInstanceId));

        try {
            ServiceDefinitionProxy service = ecs
                    .lookupServiceDefinition(serviceDefinitionId);
            PlanProxy plan = service.findPlan(planId);
            InstanceWorkflow workflow = getWorkflow(request)
                    .withCreateRequest(request);

            LOG.info("creating service instance");
            ServiceInstance instance =
                    workflow.create(serviceInstanceId, service, plan, request.getParameters());

            LOG.info("saving instance...");
            repository.save(instance);

            return Mono.just(CreateServiceInstanceResponse.builder()
                    .async(false)
                    .build());
        } catch (Exception e) {
            logger.error(format("Unexpected error creating service %s", serviceInstanceId), e);
            throw new ServiceBrokerException(e);
        }
    }

    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {

        String serviceInstanceId = request.getServiceInstanceId();
        String serviceDefinitionId = request.getServiceDefinitionId();
        try {
            ServiceDefinitionProxy service = ecs
                    .lookupServiceDefinition(serviceDefinitionId);
            InstanceWorkflow workflow = getWorkflow(service)
                    .withDeleteRequest(request);

            LOG.info("deleting service instance");
            workflow.delete(serviceInstanceId);

            LOG.info("removing instance from repo");
            repository.delete(serviceInstanceId);

            return Mono.just(DeleteServiceInstanceResponse.builder()
                    .async(false)
                    .build());
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    @Override
    public Mono<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request) {
        String serviceInstanceId = request.getServiceInstanceId();
        String serviceDefinitionId = request.getServiceDefinitionId();
        String planId = request.getPlanId();
        try {
            ServiceDefinitionProxy service = ecs
                    .lookupServiceDefinition(serviceDefinitionId);
            ServiceInstance instance = repository.find(serviceInstanceId);
            if (instance == null)
                throw new ServiceInstanceDoesNotExistException(
                        serviceInstanceId);
            if (instance.getReferences().size() > 1)
                throw new ServiceInstanceUpdateNotSupportedException(
                        "Cannot change plan of service instance with remote references");

            InstanceWorkflow workflow = getWorkflow(service);
            LOG.info("changing instance plan");
            Map<String, Object> serviceSettings =
                    workflow.changePlan(serviceInstanceId, service, service.findPlan(planId), request.getParameters());

            LOG.info("updating service in repo");
            // This shouldn't be needed. The object will be re-versioned
            // repository.delete(serviceInstanceId);
            instance.update(request, serviceSettings);
            repository.save(instance);
            return Mono.just(UpdateServiceInstanceResponse.builder()
                    .async(false)
                    .build());
        } catch (ServiceInstanceDoesNotExistException e) {
            // Rethrow "does not exist" so that it's not caught by the generic case
            throw e;
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    private InstanceWorkflow getWorkflow(CreateServiceInstanceRequest createRequest)
            throws EcsManagementClientException {
        if (isRemoteConnection(createRequest))
            return new RemoteConnectionInstanceWorkflow(repository, ecs);
        ServiceDefinitionProxy service = ecs.lookupServiceDefinition(createRequest.getServiceDefinitionId());
        return getWorkflow(service);
    }

    private boolean isRemoteConnection(CreateServiceInstanceRequest createRequest) {
        Map<String, Object> parameters = createRequest.getParameters();
        return parameters != null && parameters.containsKey("remote_connection");
    }

    private InstanceWorkflow getWorkflow(ServiceDefinitionProxy service)
            throws EcsManagementClientException {
        String serviceType = (String) service.getServiceSettings().get(SERVICE_TYPE);
        switch (serviceType) {
            case NAMESPACE:
                return new NamespaceInstanceWorkflow(repository, ecs);
            case BUCKET:
                return new BucketInstanceWorkflow(repository, ecs);
            default:
                throw new ServiceBrokerException(NO_SERVICE_MATCHING_TYPE +
                        serviceType);
        }
    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(
            GetLastServiceOperationRequest request) {
        return Mono.just(GetLastServiceOperationResponse.builder()
                .operationState(OperationState.SUCCEEDED)
                .build());
    }
}