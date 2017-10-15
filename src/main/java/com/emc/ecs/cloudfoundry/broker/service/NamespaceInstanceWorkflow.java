package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NamespaceInstanceWorkflow extends InstanceWorkflowImpl {
    NamespaceInstanceWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        super(instanceRepo, ecs);
    }

    @Override
    public void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> maybeParameters) throws EcsManagementClientException, IOException {
        Map<String, Object> parameters = maybeParameters
                .orElse(new HashMap<>());
        ecs.changeNamespacePlan(id, service, plan, parameters);
    }

    @Override
    public void delete(String id) {
        try {
            ServiceInstance instance = instanceRepository.find(id);
            if (instance.getReferences().size() > 1) {
                removeInstanceFromReferences(instance, id);
            } else {
                ecs.deleteNamespace(id);
            }
        } catch (EcsManagementClientException | JAXBException | IOException e) {
            throw new ServiceBrokerException(e);
        }
    }

    private void removeInstanceFromReferences(ServiceInstance instance, String id) throws IOException, JAXBException {
        for (String refId : instance.getReferences()) {
            if (!refId.equals(id)) {
                ServiceInstance ref = instanceRepository.find(refId);
                Set<String> references = ref.getReferences()
                        .stream()
                        .filter((String i) -> ! i.equals(id))
                        .collect(Collectors.toSet());
                ref.setReferences(references);
                instanceRepository.save(ref);
            }
        }
     }

    @Override
    public ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> parameters) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
        ecs.createNamespace(id, service, plan, parameters);
        return getServiceInstance();
    }
}
