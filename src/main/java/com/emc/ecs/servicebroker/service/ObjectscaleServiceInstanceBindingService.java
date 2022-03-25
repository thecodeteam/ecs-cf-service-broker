package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.io.IOException;

public class ObjectscaleServiceInstanceBindingService extends EcsServiceInstanceBindingService {
    @Override
    protected BindingWorkflow getWorkflow(ServiceDefinitionProxy service) throws IOException {
        ServiceType serviceType = ServiceType.fromSettings(service.getServiceSettings());

        if (serviceType == ServiceType.BUCKET) {
            return new BucketBindingWorkflow(instanceRepo, ecs);
        }

        throw new ServiceBrokerException("Unsupported service type: " + serviceType);
    }
}
