package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class NamespaceInstanceWorkflow extends InstanceWorkflowImpl {
    public NamespaceInstanceWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> maybeParameters) throws EcsManagementClientException, IOException {
        ServiceInstance inst = instanceRepository.find(id);
        if (inst.getReferences().size() > 1)
            throw new ServiceBrokerInvalidParametersException("Cannot change plan of namespace with remote references");
        ecs.changeNamespacePlan(id, service, plan, maybeParameters.get());
    }

    @Override
    public void delete(String id) throws EcsManagementClientException {
        // FIXME: Check for multiple remote references before deleting
        ecs.deleteNamespace(id);
    }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> parameters) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
        ecs.createNamespace(id, service, plan, parameters);
        return getServiceInstance();
    }
}
