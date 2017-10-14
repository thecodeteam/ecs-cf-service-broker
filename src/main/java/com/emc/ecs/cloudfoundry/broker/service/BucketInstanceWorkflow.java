package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BucketInstanceWorkflow extends InstanceWorkflowImpl {
    protected BucketInstanceWorkflow(ServiceInstanceRepository repo, EcsService ecs) {
        super(repo, ecs);
    }

    @Override
    public void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> maybeParameters)
            throws EcsManagementClientException {
        // FIXME: Check for multiple remote references -- we won't allow changing plans for those...
        Map<String, Object> parameters = maybeParameters
                .orElse(new HashMap<>());
        ecs.changeBucketPlan(id, service, plan, parameters);
    }

    @Override
    public void delete(String id) throws EcsManagementClientException {
        // FIXME: Check for multiple remote references before deleting
        ecs.deleteBucket(id);
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
