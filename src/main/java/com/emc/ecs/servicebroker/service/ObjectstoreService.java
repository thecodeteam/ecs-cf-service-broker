package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.ObjectscaleGatewayConnection;
import com.emc.ecs.management.sdk.actions.*;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ReclaimPolicy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.tool.BucketWipeOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECTSCALE;
import static com.emc.ecs.servicebroker.model.Constants.*;

@Service
public class ObjectstoreService {
    private static final Logger logger = LoggerFactory.getLogger(ObjectstoreService.class);

    @Autowired
    @Qualifier("managementAPI")
    private ManagementAPIConnection managementAPI;

    @Autowired
    private ObjectscaleGatewayConnection objectscaleGateway;

    @Autowired
    private BrokerConfig broker;

    @Autowired
    private CatalogConfig catalog;

    private String objectEndpoint;

    @Autowired
    private BucketWipeFactory bucketWipeFactory;

    private BucketWipeOperations bucketWipe;

    @PostConstruct
    void initialize() {
        if (OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            logger.info("Initializing Objectstore service with management endpoint {}", broker.getObjectstoreManagementEndpoint());

            try {
                prepareRepository();
/*
                prepareBucketWipe();
*/
            } catch (EcsManagementClientException e) {
                logger.error("Failed to initialize Objectscale service: {}", e.getMessage());
                throw new ServiceBrokerException(e.getMessage(), e);
            }

        }
    }

    String prefix(String string) {
        return broker.getPrefix() + string;
    }

    private void prepareRepository() throws EcsManagementClientException {
        String bucketName = broker.getRepositoryBucket();
        String namespace = broker.getAccountId();

        if (!bucketExists(bucketName, namespace)) {
            logger.info("Preparing repository bucket '{}'", prefix(bucketName));

            ServiceDefinitionProxy service;
            try {
                if (broker.getRepositoryServiceId() == null) {
                    service = catalog.getRepositoryServiceDefinition();
                } else {
                    service = catalog.findServiceDefinition(broker.getRepositoryServiceId());
                }

                PlanProxy plan;
                if (broker.getRepositoryPlanId() == null) {
                    plan = service.getRepositoryPlan();
                } else {
                    plan = service.findPlan(broker.getRepositoryPlanId());
                }

                Map<String, Object> parameters = new HashMap<>();
                parameters.put(NAMESPACE, namespace);

                createBucket("repository", bucketName, service, plan, parameters);
            } catch (ServiceBrokerException e) {
                String errorMessage = "Failed to create broker repository bucket: " + e.getMessage();
                logger.error(errorMessage);
                throw new ServiceBrokerException(errorMessage, e);
            }
        }
    }

    public boolean bucketExists(String bucketName, String namespace) throws EcsManagementClientException {
        return BucketAction.exists(managementAPI, prefix(bucketName), namespace);
    }

    Map<String, Object> createBucket(String serviceInstanceId, String bucketName, ServiceDefinitionProxy serviceDefinition,
                                     PlanProxy plan, Map<String, Object> parameters) {
        try {
            parameters = EcsService.mergeParameters(broker, serviceDefinition, plan, parameters);
            //parameters = validateAndPrepareSearchMetadata(parameters);

            // Validate the reclaim-policy
            if (!ReclaimPolicy.isPolicyAllowed(parameters)) {
                throw new ServiceBrokerInvalidParametersException("Reclaim Policy " + ReclaimPolicy.getReclaimPolicy(parameters) + " is not one of the allowed polices " + ReclaimPolicy.getAllowedReclaimPolicies(parameters));
            }

            // Validate expiration policy
            if (parameters.containsKey(EXPIRATION) && parameters.get(EXPIRATION) != null) {
                if ((boolean) parameters.get(FILE_ACCESSIBLE)) {
                    throw new ServiceBrokerInvalidParametersException("Cannot apply expiration rule to file accessible bucket");
                }
            }

            logger.info("Creating bucket '{}' with service '{}' plan '{}'({}) and params {}", prefix(bucketName), serviceDefinition.getName(), plan.getName(), plan.getId(), parameters);

            String namespace = (String) parameters.get(NAMESPACE);

            if (bucketExists(bucketName, namespace)) {
                throw new ServiceInstanceExistsException(serviceInstanceId, serviceDefinition.getId());
            }

            DataServiceReplicationGroup replicationGroup = lookupReplicationGroup((String) parameters.get(REPLICATION_GROUP));

            if (replicationGroup == null) {
                throw new ServiceBrokerException("Cannot create bucket - replication group '" + parameters.get(REPLICATION_GROUP) + "' not found");
            }

            BucketAction.create(managementAPI, new ObjectBucketCreate(
                    prefix(bucketName),
                    namespace,
                    replicationGroup.getId(),
                    parameters
            ));

            if (parameters.containsKey(QUOTA) && parameters.get(QUOTA) != null) {
                Map<String, Integer> quota = (Map<String, Integer>) parameters.get(QUOTA);
                logger.info("Applying bucket quota on '{}' in '{}': limit {}, warn {}", prefix(bucketName), namespace, quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
                BucketQuotaAction.create(managementAPI, namespace, prefix(bucketName), quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
            }

            if (parameters.containsKey(DEFAULT_RETENTION) && parameters.get(DEFAULT_RETENTION) != null) {
                logger.info("Applying bucket retention policy on '{}' in '{}': {}", bucketName, namespace, parameters.get(DEFAULT_RETENTION));
                BucketRetentionAction.update(managementAPI, namespace, prefix(bucketName), (int) parameters.get(DEFAULT_RETENTION));
            }

            if (parameters.containsKey(TAGS) && parameters.get(TAGS) != null) {
                List<Map<String, String>> bucketTags = (List<Map<String, String>>) parameters.get(TAGS);
                logger.info("Applying bucket tags on '{}': {}", bucketName, bucketTags);
                BucketTagsAction.create(managementAPI, prefix(bucketName), new BucketTagsParamAdd(namespace, bucketTags));
            }

            /* TODO
            if (parameters.containsKey(EXPIRATION) && parameters.get(EXPIRATION) != null) {
                grantUserLifecycleManagementPolicy(prefix(bucketName), namespace, prefix(broker.getRepositoryUser()));
                logger.info("Applying bucket expiration on '{}': {} days", bucketName, parameters.get(EXPIRATION));
                BucketExpirationAction.update(broker, namespace, prefix(bucketName), (int) parameters.get(EXPIRATION), null);
            }
             */
        } catch (Exception e) {
            String errorMessage = String.format("Failed to create bucket '%s': %s", bucketName, e.getMessage());
            logger.error(errorMessage, e);
            throw new ServiceBrokerException(errorMessage, e);
        }
        return parameters;
    }

    public DataServiceReplicationGroup lookupReplicationGroup(String replicationGroup) throws EcsManagementClientException {
        return ReplicationGroupAction.list(managementAPI).stream()
                .filter(r -> replicationGroup != null && r != null
                        && (replicationGroup.equals(r.getName()) || replicationGroup.equals(r.getId()))
                )
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("ECS replication group not found: " + replicationGroup));
    }
}
