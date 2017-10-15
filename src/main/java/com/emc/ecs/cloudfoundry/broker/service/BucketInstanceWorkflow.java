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
import java.util.*;
import java.util.stream.Collectors;

public class BucketInstanceWorkflow extends InstanceWorkflowImpl {
    BucketInstanceWorkflow(ServiceInstanceRepository repo, EcsService ecs) {
        super(repo, ecs);
    }

    @Override
    public void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> maybeParameters)
            throws EcsManagementClientException, IOException {
        Map<String, Object> parameters = maybeParameters
                .orElse(new HashMap<>());
        ecs.changeBucketPlan(id, service, plan, parameters);
    }

    @Override
    public void delete(String id) throws EcsManagementClientException, IOException, ServiceBrokerException {
        ServiceInstance instance = instanceRepository.find(id);
        if (instance.getReferences().size() > 1) {
            try {
                removeInstanceFromReferences(instance, id);
            } catch (Exception e) {
                throw new ServiceBrokerException(e);
            }
        } else {
            ecs.deleteBucket(id);
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
    public ServiceInstance create(String bucketName, ServiceDefinitionProxy service, PlanProxy plan,
                                  Optional<Map<String, Object>> maybeParameters)
            throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException {
        Map<String, Object> parameters = maybeParameters
                .orElse(new HashMap<>());
        ecs.createBucket(bucketName, service, plan, parameters);

        return getServiceInstance();
    }
}
