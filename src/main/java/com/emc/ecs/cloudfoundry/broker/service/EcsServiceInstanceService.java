package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

@Service
public class EcsServiceInstanceService implements ServiceInstanceService {
    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
    private static final String NAMESPACE = "namespace";
    private static final String BUCKET = "bucket";
    private static final String SERVICE_TYPE = "service-type";
    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceService.class);

    @Autowired
    private EcsService ecs;

    @Autowired
    private ServiceInstanceRepository repository;

    public EcsServiceInstanceService() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException, URISyntaxException {
        super();
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
        String serviceInstanceId = request.getServiceInstanceId();
        String serviceDefinitionId = request.getServiceDefinitionId();
        String planId = request.getPlanId();
        try {
            ServiceDefinitionProxy service = ecs
                    .lookupServiceDefinition(serviceDefinitionId);
            PlanProxy plan = service.findPlan(planId);
            InstanceWorkflow workflow = getWorkflow(service)
                    .withCreateRequest(request);

            LOG.info("creating service instance");
            ServiceInstance instance = workflow.create(serviceInstanceId, service, plan, request.getParameters());

            LOG.info("saving instance...");
            repository.save(instance);

            return new CreateServiceInstanceResponse();
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {

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

            return new DeleteServiceInstanceResponse();
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
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
            workflow.changePlan(serviceInstanceId, service, service.findPlan(planId), request.getParameters());

            LOG.info("updating service in repo");
            repository.delete(serviceInstanceId);
            instance.update(request);
            repository.save(instance);
            return new UpdateServiceInstanceResponse();
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
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
    public GetLastServiceOperationResponse getLastOperation(
            GetLastServiceOperationRequest request) {
        return new GetLastServiceOperationResponse();
    }
}