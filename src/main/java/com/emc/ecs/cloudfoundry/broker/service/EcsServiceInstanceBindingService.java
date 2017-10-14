package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

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

    @Autowired
    private ServiceInstanceRepository instanceRepo;

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
            BindingWorkflow workflow = getWorkflow(request);

            LOG.info("creating binding");
            workflow.checkIfUserExists();
            String secretKey = workflow.createBindingUser();

            LOG.info("building binding response");
            Map<String, Object> credentials = workflow.getCredentials(secretKey);
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
            BindingWorkflow workflow = getWorkflow(request)
                    .withDeleteRequest(request);

            LOG.info("looking up binding: " + bindingId);
            ServiceInstanceBinding binding = repository.find(bindingId);
            if (binding == null)
                throw new ServiceInstanceBindingDoesNotExistException(bindingId);
            LOG.info("binding found: " + bindingId);

            workflow.removeBinding(binding);

            LOG.info("deleting from repository" + bindingId);
            repository.delete(bindingId);
        } catch (Exception e) {
            LOG.error("Error deleting binding: " + e);
            throw new ServiceBrokerException(e);
        }
    }

    private BindingWorkflow getWorkflow(DeleteServiceInstanceBindingRequest deleteRequest)
            throws EcsManagementClientException, IOException {

        if (isRemoteConnectBinding(deleteRequest))
            return new RemoteConnectBindingWorkflow(instanceRepo, ecs);

        ServiceDefinitionProxy service =
                ecs.lookupServiceDefinition(deleteRequest.getServiceDefinitionId());
        return getWorkflow(service).withDeleteRequest(deleteRequest);
    }

    private BindingWorkflow getWorkflow(CreateServiceInstanceBindingRequest createRequest)
            throws EcsManagementClientException, IOException {

        if (isRemoteConnectBinding(createRequest))
            return new RemoteConnectBindingWorkflow(instanceRepo, ecs)
                    .withCreateRequest(createRequest);

        ServiceDefinitionProxy service =
                ecs.lookupServiceDefinition(createRequest.getServiceDefinitionId());
        return getWorkflow(service).withCreateRequest(createRequest);
    }

    private BindingWorkflow getWorkflow(ServiceDefinitionProxy service)
            throws IOException, EcsManagementClientException {
        String serviceType = (String) service.getServiceSettings().get(SERVICE_TYPE);
        switch (serviceType) {
            case NAMESPACE:
                return new NamespaceBindingWorkflow(instanceRepo, ecs);
            case BUCKET:
                return new BucketBindingWorkflow(instanceRepo, ecs);
            default:
                throw new ServiceBrokerException(NO_SERVICE_MATCHING_TYPE + serviceType);
        }
    }

    private boolean isRemoteConnectBinding(DeleteServiceInstanceBindingRequest deleteRequest)
            throws EcsManagementClientException, IOException {
        String bindingId = deleteRequest.getBindingId();
        ServiceInstanceBinding binding = repository.find(bindingId);
        if (binding == null)
            throw new ServiceInstanceBindingDoesNotExistException(bindingId);
        return isRemoteConnectBinding(binding.getParameters());
    }

    private Boolean isRemoteConnectBinding(CreateServiceInstanceBindingRequest createRequest) {
        Map<String, Object> parameters = createRequest.getParameters();
        return isRemoteConnectBinding(parameters);
    }

    private boolean isRemoteConnectBinding(Map<String, Object> parameters) {
        if (parameters == null)
            return false;
        if (parameters.containsKey("connect_remote"))
            return (Boolean) parameters.get("connect_remote");
        return false;
    }

}
