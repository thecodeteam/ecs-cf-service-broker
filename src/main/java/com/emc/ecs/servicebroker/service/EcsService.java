package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.*;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ReclaimPolicy;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.tool.BucketWipeOperations;
import com.emc.ecs.tool.BucketWipeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EcsService {

    private static final Logger logger = LoggerFactory.getLogger(EcsService.class);

    private static final String UNCHECKED = "unchecked";
    private static final String WARN = "warn";
    private static final String LIMIT = "limit";
    private static final String QUOTA = "quota";
    private static final String RETENTION = "retention";
    private static final String SERVICE_NOT_FOUND =
            "No service matching service id: ";
    private static final String DEFAULT_RETENTION = "default-retention";
    private static final String INVALID_RECLAIM_POLICY = "Invalid reclaim-policy: ";
    private static final String INVALID_ALLOWED_RECLAIM_POLICIES = "Invalid reclaim-policies: ";
    private static final String REJECT_RECLAIM_POLICY = "Reclaim Policy is not allowed: ";

    @Autowired
    private Connection connection;

    @Autowired
    private BrokerConfig broker;

    @Autowired
    private CatalogConfig catalog;

    @Autowired
    private BucketWipeFactory bucketWipeFactory;

    private BucketWipeOperations bucketWipe;

    private String replicationGroupID;
    private String objectEndpoint;

    String getObjectEndpoint() {
        return objectEndpoint;
    }

    String getNfsMountHost() {
        return broker.getNfsMountHost();
    }

    @PostConstruct
    void initialize() {
        logger.info("Initializing ECS service with management endpoint {}, base url {}", broker.getManagementEndpoint(), broker.getBaseUrl());

        try {
            lookupObjectEndpoints();
            lookupReplicationGroupID();
            prepareDefaultReclaimPolicy();
            prepareRepository();
            prepareBucketWipe();
        } catch (EcsManagementClientException e) {
            throw new ServiceBrokerException(e);
        } catch (URISyntaxException e) {
            throw new ServiceBrokerException(e);
        }
    }

    CompletableFuture deleteBucket(String id) {
        try {
            if (bucketExists(prefix(id))) {
                BucketAction.delete(connection, prefix(id), broker.getNamespace());
            } else {
                logger.info("Bucket {} no longer exists, assume already deleted", prefix(id));
            }

            return null;
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    CompletableFuture wipeAndDeleteBucket(String id) {
        try {
            if (!bucketExists(prefix(id))) {
                logger.info("Bucket {} no longer exists, assume already deleted", prefix(id));
                return null;
            }

            addUserToBucket(id, broker.getRepositoryUser());

            logger.info("Started Wiped of bucket {}", prefix(id));
            BucketWipeResult result = bucketWipeFactory.newBucketWipeResult();
            bucketWipe.deleteAllObjects(prefix(id), "", result);

            return result.getCompletedFuture().thenRun(() -> bucketWipeCompleted(result, id));
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    Boolean getBucketFileEnabled(String id) throws EcsManagementClientException {
        ObjectBucketInfo b = BucketAction.get(connection, prefix(id), broker.getNamespace());
        return b.getFsAccessEnabled();
    }

    Map<String, Object> createBucket(String id, ServiceDefinitionProxy service,
                                     PlanProxy plan, Map<String, Object> parameters) {
        if (parameters == null) parameters = new HashMap<>();

        logger.info(String.format("Creating bucket %s", id));
        try {
            if (bucketExists(id)) {
                throw new ServiceInstanceExistsException(id, service.getId());
            }
            // merge serviceSettings into parameters, overwriting parameter values
            // with service/plan serviceSettings, since serviceSettings are forced
            // by administrator through the catalog.
            parameters.putAll(plan.getServiceSettings());
            parameters.putAll(service.getServiceSettings());

            // Validate the reclaim-policy
            if (!ReclaimPolicy.isPolicyAllowed(parameters)) {
                throw new ServiceBrokerException("Reclaim Policy "+ReclaimPolicy.getReclaimPolicy(parameters)+" is not one of the allowed polices "+ReclaimPolicy.getAllowedReclaimPolicies(parameters));
            }

            BucketAction.create(connection, new ObjectBucketCreate(prefix(id),
                    broker.getNamespace(), replicationGroupID, parameters));

            if (parameters.containsKey(QUOTA) && parameters.get(QUOTA) != null) {
                logger.info("Applying quota");
                Map<String, Integer> quota = (Map<String, Integer>) parameters.get(QUOTA);
                BucketQuotaAction.create(connection, prefix(id), broker.getNamespace(), quota.get(LIMIT), quota.get(WARN));
            }

            if (parameters.containsKey(DEFAULT_RETENTION) && parameters.get(DEFAULT_RETENTION) != null) {
                logger.info("Applying retention policy");
                BucketRetentionAction.update(connection, broker.getNamespace(),
                        prefix(id), (int) parameters.get(DEFAULT_RETENTION));
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to create bucket %s", id), e);
            throw new ServiceBrokerException(e);
        }
        return parameters;
    }

    Map<String, Object> changeBucketPlan(String id, ServiceDefinitionProxy service,
                                         PlanProxy plan, Map<String, Object> parameters) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        // merge serviceSettings into parameters, overwriting parameter values
        // with service/plan serviceSettings, since serviceSettings are forced
        // by administrator through the catalog.
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());

        // Validate the reclaim-policy
        validateReclaimPolicy(parameters);

        @SuppressWarnings(UNCHECKED)
        Map<String, Object> quota = (Map<String, Object>) parameters
                .getOrDefault(QUOTA, new HashMap<>());
        int limit = (int) quota.getOrDefault(LIMIT, -1);
        int warn = (int) quota.getOrDefault(WARN, -1);

        try {
            if (limit == -1 && warn == -1) {
                parameters.remove(QUOTA);
                BucketQuotaAction.delete(connection, prefix(id),
                        broker.getNamespace());
            } else {
                BucketQuotaAction.create(connection, prefix(id),
                        broker.getNamespace(), limit, warn);
            }
        } catch (EcsManagementClientException e) {
            throw new ServiceBrokerException(e);
        }
        return parameters;
    }

    private boolean bucketExists(String id) throws EcsManagementClientException {
        return BucketAction.exists(connection, prefix(id),
                broker.getNamespace());
    }

    private boolean aclExists(String id) throws EcsManagementClientException {
        return BucketAclAction.exists(connection, prefix(id),
            broker.getNamespace());
    }

    UserSecretKey createUser(String id) {
        try {
            logger.info(String.format("Creating user %s", prefix(id)));
            ObjectUserAction.create(connection, prefix(id), broker.getNamespace());
            logger.info(String.format("Creating secret for user %s", prefix(id)));
            ObjectUserSecretAction.create(connection, prefix(id));
            return ObjectUserSecretAction.list(connection, prefix(id)).get(0);
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    UserSecretKey createUser(String id, String namespace)
            throws EcsManagementClientException {
        ObjectUserAction.create(connection, prefix(id), prefix(namespace));
        ObjectUserSecretAction.create(connection, prefix(id));
        return ObjectUserSecretAction.list(connection, prefix(id)).get(0);
    }

    void createUserMap(String id, int uid)
            throws EcsManagementClientException {
        ObjectUserMapAction.create(connection, prefix(id), uid, broker.getNamespace());
    }

    void deleteUserMap(String id, String uid)
            throws EcsManagementClientException {
        ObjectUserMapAction.delete(connection, prefix(id), uid, broker.getNamespace());
    }

    Boolean userExists(String id) throws ServiceBrokerException {
        try {
            return ObjectUserAction.exists(connection, prefix(id),
                    broker.getNamespace());
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    void deleteUser(String id) throws EcsManagementClientException {
        if (userExists(id)) {
            ObjectUserAction.delete(connection, prefix(id));
        } else {
            logger.info("User {} no longer exists, assume already deleted", id);
        }
    }

    void addUserToBucket(String id, String username) {
        logger.info(String.format("Adding user %s to bucket %s", username, id));
        try {
            addUserToBucket(id, username, Collections.singletonList("full_control"));
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    void addUserToBucket(String id, String username,
                         List<String> permissions) throws EcsManagementClientException {
        logger.info("Adding user {} to bucket {}", prefix(username), prefix(id));
        BucketAcl acl = BucketAclAction.get(connection, prefix(id),
                broker.getNamespace());
        List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
        userAcl.add(new BucketUserAcl(prefix(username), permissions));
        acl.getAcl().setUserAccessList(userAcl);
        BucketAclAction.update(connection, prefix(id), acl);

        if (!getBucketFileEnabled(id)) {
            BucketPolicy bucketPolicy = new BucketPolicy(
                    "2012-10-17",
                    "DefaultPCFBucketPolicy",
                    new BucketPolicyStatement("DefaultAllowTotalAccess",
                            new BucketPolicyEffect("Allow"),
                            new BucketPolicyPrincipal(prefix(username)),
                            new BucketPolicyActions(Collections.singletonList("s3:*")),
                            new BucketPolicyResource(Collections.singletonList(prefix(id)))
                    )
            );
            BucketPolicyAction.update(connection, prefix(id), bucketPolicy, broker.getNamespace());
        }
    }

    void removeUserFromBucket(String id, String username)
            throws EcsManagementClientException {
        if (!aclExists(id)) {
            logger.info("ACL {} no longer exists when removing user {}", prefix(id), prefix(username));
            return;
        }

        BucketAcl acl = BucketAclAction.get(connection, prefix(id),
                broker.getNamespace());
        List<BucketUserAcl> newUserAcl = acl.getAcl().getUserAccessList()
                .stream().filter(a -> !a.getUser().equals(prefix(username)))
                .collect(Collectors.toList());
        acl.getAcl().setUserAccessList(newUserAcl);
        BucketAclAction.update(connection, prefix(id), acl);
    }

    String prefix(String string) {
        return broker.getPrefix() + string;
    }

    private void lookupObjectEndpoints() throws EcsManagementClientException {
        if (broker.getObjectEndpoint() != null) {
            objectEndpoint = broker.getObjectEndpoint();
        } else {
            List<BaseUrl> baseUrlList = BaseUrlAction.list(connection);
            String urlId;

            if (baseUrlList.isEmpty()) {
                throw new ServiceBrokerException(
                        "No object endpoint or base URL available");
            } else if (broker.getBaseUrl() != null) {
                urlId = baseUrlList.stream()
                        .filter(b -> broker.getBaseUrl().equals(b.getName()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceBrokerException("configured ECS Base URL not found"))
                        .getId();
            } else {
                urlId = detectDefaultBaseUrlId(baseUrlList);
            }

            objectEndpoint = BaseUrlAction.get(connection, urlId)
                    .getNamespaceUrl(broker.getNamespace(), false);
        }
        if (broker.getRepositoryEndpoint() == null)
            broker.setRepositoryEndpoint(objectEndpoint);
    }

    String getNamespaceURL(String namespace, Map<String, Object> parametersAbstract, Map<String, Object> serviceSettings) {
        HashMap<String, Object> parameters =
                (parametersAbstract instanceof HashMap)
                        ? (HashMap) parametersAbstract
                        : new HashMap<>(parametersAbstract);
        if (serviceSettings != null) {
            // merge serviceSettings into parameters, overwriting parameter values
            // with serviceSettings, since serviceSettings are forced by administrator
            // through the catalog.
            parameters.putAll(serviceSettings);
        }
        try {
            return getNamespaceURL(namespace, parameters);
        } catch (EcsManagementClientException e) {
            throw new ServiceBrokerException(e);
        }
    }

    private String getNamespaceURL(String namespace, Map<String, Object> parameters) throws EcsManagementClientException {
        String baseUrl = (String) parameters.getOrDefault("base-url", broker.getBaseUrl());
        Boolean useSSL = (Boolean) parameters.getOrDefault("use-ssl", false);
        return getNamespaceURL(namespace, useSSL, baseUrl);
    }

    private String getNamespaceURL(String namespace, Boolean useSSL, String baseURL)
            throws EcsManagementClientException {
        List<BaseUrl> baseUrlList = BaseUrlAction.list(connection);
        String urlId = baseUrlList.stream()
                .filter(b -> baseURL.equals(b.getName()))
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("Configured ECS namespace not found."))
                .getId();
        return BaseUrlAction.get(connection, urlId).getNamespaceUrl(namespace, useSSL);
    }

    private void lookupReplicationGroupID()
            throws EcsManagementClientException {
        replicationGroupID = ReplicationGroupAction.list(connection).stream()
                .filter(r -> broker.getReplicationGroup().equals(r.getName()))
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("Configured ECS replication group not found."))
                .getId();
    }

    private void prepareRepository() throws EcsManagementClientException {
        String bucketName = broker.getRepositoryBucket();
        String userName = broker.getRepositoryUser();
        if (!bucketExists(bucketName)) {
            ServiceDefinitionProxy service;
            if (broker.getRepositoryServiceId() == null) {
                service = catalog.getRepositoryService();
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
            createBucket(bucketName, service, plan, parameters);
        }

        if (!userExists(userName)) {
            UserSecretKey secretKey = createUser(userName);
            addUserToBucket(bucketName, userName);
            broker.setRepositorySecret(secretKey.getSecretKey());
        } else {
            broker.setRepositorySecret(getUserSecret(userName));
        }
    }

    private void prepareBucketWipe() throws URISyntaxException {
        bucketWipe = bucketWipeFactory.getBucketWipe(broker);
    }

    private void prepareDefaultReclaimPolicy() {
        String defaultReclaimPolicy = broker.getDefaultReclaimPolicy();
        if (defaultReclaimPolicy != null) {
            ReclaimPolicy.DEFAULT_RECLAIM_POLICY = ReclaimPolicy.valueOf(defaultReclaimPolicy);
        }
        logger.info("Default Reclaim Policy: " + ReclaimPolicy.DEFAULT_RECLAIM_POLICY);
    }

    private String getUserSecret(String id)
            throws EcsManagementClientException {
        return ObjectUserSecretAction.list(connection, prefix(id)).get(0)
                .getSecretKey();
    }

    private String detectDefaultBaseUrlId(List<BaseUrl> baseUrlList) {
        Optional<BaseUrl> maybeBaseUrl = baseUrlList.stream()
                .filter(b -> "DefaultBaseUrl".equals(b.getName())).findAny();
        if (maybeBaseUrl.isPresent()) {
            return maybeBaseUrl.get().getId();
        }
        return baseUrlList.get(0).getId();
    }

    private Boolean namespaceExists(String id) throws EcsManagementClientException {
        return NamespaceAction.exists(connection, id);
    }

    private void validateReclaimPolicy(Map<String, Object> parameters) {
        // Ensure Reclaim-Policy can be parsed
        try {
            ReclaimPolicy.getReclaimPolicy(parameters);
        } catch(IllegalArgumentException e) {
            throw new ServiceBrokerException(INVALID_RECLAIM_POLICY + ReclaimPolicy.getReclaimPolicy(parameters));
        }

        // Ensure Allowed-Reclaim-Policies can be parsed
        try {
            ReclaimPolicy.getAllowedReclaimPolicies(parameters);
        } catch(IllegalArgumentException e) {
            throw new ServiceBrokerException(INVALID_ALLOWED_RECLAIM_POLICIES + ReclaimPolicy.getReclaimPolicy(parameters));
        }

        if (!ReclaimPolicy.isPolicyAllowed(parameters)) {
            throw new ServiceBrokerException(REJECT_RECLAIM_POLICY + ReclaimPolicy.getReclaimPolicy(parameters));
        }
    }

    Map<String, Object> createNamespace(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters)
            throws EcsManagementClientException {
        if (namespaceExists(prefix(id))) {
            throw new ServiceInstanceExistsException(id, service.getId());
        }

        if (parameters == null) parameters = new HashMap<>();
        // merge serviceSettings into parameters, overwriting parameter values
        // with service/plan serviceSettings, since serviceSettings are forced
        // by administrator through the catalog.
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());

        NamespaceAction.create(connection, new NamespaceCreate(prefix(id), replicationGroupID, parameters));

        if (parameters.containsKey(QUOTA)) {
            @SuppressWarnings(UNCHECKED)
            Map<String, Integer> quota = (Map<String, Integer>) parameters
                    .get(QUOTA);
            NamespaceQuotaParam quotaParam = new NamespaceQuotaParam(id,
                    quota.get(LIMIT), quota.get(WARN));
            NamespaceQuotaAction.create(connection, prefix(id), quotaParam);
        }

        if (parameters.containsKey(RETENTION)) {
            @SuppressWarnings(UNCHECKED)
            Map<String, Integer> retention = (Map<String, Integer>) parameters
                    .get(RETENTION);
            for (Map.Entry<String, Integer> entry : retention.entrySet()) {
                NamespaceRetentionAction.create(connection, prefix(id),
                        new RetentionClassCreate(entry.getKey(),
                                entry.getValue()));
            }
        }
        return parameters;
    }

    void deleteNamespace(String id) throws EcsManagementClientException {
        if (namespaceExists(prefix((id)))) {
            NamespaceAction.delete(connection, prefix(id));
        } else {
            logger.info("Namespace {} no longer exists, assume already deleted", prefix(id));
        }
    }

    /**
     * Handle extra steps after a bucket wipe has completed.
     *
     * Throwing an exception here will throw an exception in the CompletableFuture pipeline to signify the operation failed
     */
    private void bucketWipeCompleted(BucketWipeResult result, String id) {
        // Wipe Failed, mark as error
        if (!result.getErrors().isEmpty()) {
            logger.error("BucketWipe FAILED, deleted {} objects. Leaving bucket {}", result.getDeletedObjects(), prefix(id));
            result.getErrors().forEach(error -> logger.error("BucketWipe {} error: {}", prefix(id), error));

            throw new RuntimeException("BucketWipe Failed with "+result.getErrors().size()+" errors: "+result.getErrors().get(0));
        }

        // Wipe Succeeded, Attempt Bucket Delete
        try {
            logger.info("BucketWipe SUCCEEDED, deleted {} objects, Deleting bucket {}", result.getDeletedObjects(), prefix(id));
            deleteBucket(id);
        } catch (Exception e) {
            logger.error("Error deleting bucket "+prefix(id), e);
            throw new RuntimeException("Error Deleting Bucket "+prefix(id)+" "+e.getMessage());
        }
    }

    Map<String, Object> changeNamespacePlan(String id, ServiceDefinitionProxy service,
                                            PlanProxy plan, Map<String, Object> parameters)
            throws EcsManagementClientException {
        // merge serviceSettings into parameters, overwriting parameter values
        // with service/plan serviceSettings, since serviceSettings are forced
        // by administrator through the catalog.
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());
        NamespaceAction.update(connection, prefix(id),
                new NamespaceUpdate(parameters));

        if (parameters.containsKey(RETENTION)) {
            @SuppressWarnings(UNCHECKED)
            Map<String, Integer> retention = (Map<String, Integer>) parameters
                    .get(RETENTION);
            for (Map.Entry<String, Integer> entry : retention.entrySet()) {
                if (NamespaceRetentionAction.exists(connection, id, entry.getKey())) {
                    if (-1 == entry.getValue()) {
                        NamespaceRetentionAction.delete(connection, prefix(id), entry.getKey());
                        parameters.remove(RETENTION);
                    } else {
                        NamespaceRetentionAction.update(connection, prefix(id), entry.getKey(),
                                new RetentionClassUpdate(entry.getValue()));
                    }
                } else {
                    NamespaceRetentionAction.create(connection, prefix(id),
                            new RetentionClassCreate(entry.getKey(), entry.getValue()));
                }
            }
        }
        return parameters;
    }

    ServiceDefinitionProxy lookupServiceDefinition(
            String serviceDefinitionId) throws ServiceBrokerException {
        ServiceDefinitionProxy service = catalog
                .findServiceDefinition(serviceDefinitionId);
        if (service == null)
            throw new ServiceBrokerException(SERVICE_NOT_FOUND + serviceDefinitionId);
        return service;
    }

    String addExportToBucket(String instanceId, String relativeExportPath) throws EcsManagementClientException {
        if (relativeExportPath == null)
            relativeExportPath = "";
        String namespace = broker.getNamespace();
        String absoluteExportPath = "/" + namespace + "/" + prefix(instanceId) + "/" + relativeExportPath;
        List<NFSExport> exports = NFSExportAction.list(connection, absoluteExportPath);
        if (exports == null) {
            NFSExportAction.create(connection, absoluteExportPath);
        }
        return absoluteExportPath;
    }
}
