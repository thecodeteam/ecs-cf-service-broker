package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EcsServiceInstanceBindingService
        implements ServiceInstanceBindingService {
    private static final String NAMESPACE = "namespace";
    private static final String BUCKET = "bucket";

    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);
    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
    private static final String SERVICE_TYPE = "service-type";

    @Autowired
    private EcsService ecs;

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
        try {
            ServiceDefinitionProxy service = ecs.lookupServiceDefinition(request.getServiceDefinitionId());
            BindingWorkflow workflow = getWorkflow(service)
                    .withCreateRequest(request);

            LOG.info("creating binding");
            workflow.checkIfUserExists();
            UserSecretKey secretKey = workflow.createBindingUser();

            LOG.info("building binding response");
            Map<String, Object> credentials = workflow.getCredentials(secretKey.getSecretKey());
            ServiceInstanceBinding binding = workflow.getBinding(credentials);

            LOG.info("saving binding...");
            repository.save(binding);
            LOG.info("binding saved.");

            return workflow.getResponse(credentials);
        } catch (IOException | JAXBException | EcsManagementClientException e) {
            throw new ServiceBrokerException(e);
        }

    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request)
            throws ServiceBrokerException {

        String bindingId = request.getBindingId();
        try {
            ServiceDefinitionProxy service = ecs.lookupServiceDefinition(
                    request.getServiceDefinitionId());
            BindingWorkflow workflow = getWorkflow(service)
                    .withDeleteRequest(request);

            LOG.info("looking up binding: " + bindingId);
            ServiceInstanceBinding binding = repository.find(bindingId);
            LOG.info("binding found: " + bindingId);

            workflow.removeBinding(binding);

            LOG.info("deleting from repository" + bindingId);
            repository.delete(bindingId);
        } catch (Exception e) {
            LOG.error("Error deleting binding: " + e);
            throw new ServiceBrokerException(e);
        }
    }

    private BindingWorkflow getWorkflow(ServiceDefinitionProxy service)
            throws EcsManagementClientException, MalformedURLException {
        String serviceType = (String) service.getServiceSettings().get(SERVICE_TYPE);
        switch (serviceType) {
            case NAMESPACE:
                return new NamespaceBindingWorkflow(ecs);
            case BUCKET:
                return new BucketBindingWorkflow(ecs);
            default:
                throw new EcsManagementClientException(NO_SERVICE_MATCHING_TYPE +
                        serviceType);
        }
    }
}
