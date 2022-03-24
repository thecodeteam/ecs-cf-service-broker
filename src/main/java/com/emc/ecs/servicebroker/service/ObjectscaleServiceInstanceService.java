package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import static com.emc.ecs.servicebroker.model.ServiceType.fromSettings;

public class ObjectscaleServiceInstanceService extends EcsServiceInstanceService {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectscaleServiceInstanceService.class);

    @Override
    protected InstanceWorkflow getWorkflow(ServiceDefinitionProxy service) throws EcsManagementClientException {
        ServiceType serviceType = fromSettings(service.getServiceSettings());
        LOG.debug("Service '{}'({}) type is {}", service.getName(), service.getId(), serviceType);

        if (serviceType == ServiceType.BUCKET) {
            return new BucketInstanceWorkflow(repository, ecs);
        }

        throw new ServiceBrokerException("Unsupported service type: " + serviceType);
    }
}
