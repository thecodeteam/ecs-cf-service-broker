package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.*;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.*;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.servicebroker.service.s3.BucketExpirationAction;
import com.emc.ecs.tool.BucketWipeOperations;
import com.emc.ecs.tool.BucketWipeResult;
import com.emc.object.s3.bean.LifecycleConfiguration;
import com.emc.object.s3.bean.LifecycleRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.emc.ecs.servicebroker.model.Constants.*;

@Service
public class EcsService {
    private static final Logger logger = LoggerFactory.getLogger(EcsService.class);

    @Autowired
    private Connection connection;

    @Autowired
    private BrokerConfig broker;

    @Autowired
    private CatalogConfig catalog;

    @Autowired
    private BucketWipeFactory bucketWipeFactory;

    private BucketWipeOperations bucketWipe;

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
        } catch (EcsManagementClientException | URISyntaxException e) {
            logger.error("Failed to initialize ECS service: {}", e.getMessage());
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    CompletableFuture deleteBucket(String bucketName, String namespace) {
        if (namespace == null) {
            // buckets created prior to ver2.1 doesnt have namespace in their settings - using old default
            namespace = broker.getNamespace();
        }
        try {
            if (namespaceExists(namespace) && bucketExists(bucketName, namespace)) {
                logger.info("Deleting bucket '{}' from namespace '{}'", prefix(bucketName), namespace);
                BucketAction.delete(connection, prefix(bucketName), namespace);
            } else {
                logger.info("Bucket '{}' no longer exists in '{}', assume already deleted", prefix(bucketName), namespace);
            }
            return null;
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    CompletableFuture wipeAndDeleteBucket(String id, String namespace) {
        if (namespace == null) {
            namespace = broker.getNamespace();
        }
        try {
            if (!namespaceExists(namespace) || !bucketExists(id, namespace)) {
                logger.info("Bucket '{}' no longer exists in '{}', assume already deleted", prefix(id), namespace);
                return null;
            }

            addUserToBucket(id, namespace, broker.getRepositoryUser());

            logger.info("Started wipe of bucket '{}' in namespace '{}'", prefix(id), namespace);
            BucketWipeResult result = bucketWipeFactory.newBucketWipeResult();
            bucketWipe.deleteAllObjects(prefix(id), "", result);

            String ns = namespace;

            return result.getCompletedFuture().thenRun(() -> bucketWipeCompleted(result, id, ns));
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    Boolean getBucketFileEnabled(String bucketName, String namespace) throws EcsManagementClientException {
        ObjectBucketInfo b = BucketAction.get(connection, prefix(bucketName), namespace);
        return b.getFsAccessEnabled();
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> createBucket(String serviceInstanceId, String bucketName, ServiceDefinitionProxy serviceDefinition,
                                     PlanProxy plan, Map<String, Object> parameters) {
        try {
            parameters = mergeParameters(broker, serviceDefinition, plan, parameters);
            parameters = validateAndPrepareSearchMetadata(parameters);

            logger.info("Creating bucket '{}' with plan '{}'({}) and params {}", prefix(bucketName), plan.getName(), plan.getId(), parameters);

            String namespace = (String) parameters.get(NAMESPACE);

            if (bucketExists(bucketName, namespace)) {
                throw new ServiceInstanceExistsException(serviceInstanceId, serviceDefinition.getId());
            }

            // Validate the reclaim-policy
            if (!ReclaimPolicy.isPolicyAllowed(parameters)) {
                throw new ServiceBrokerInvalidParametersException("Reclaim Policy " + ReclaimPolicy.getReclaimPolicy(parameters) + " is not one of the allowed polices " + ReclaimPolicy.getAllowedReclaimPolicies(parameters));
            }

            DataServiceReplicationGroup replicationGroup = lookupReplicationGroup((String) parameters.get(REPLICATION_GROUP));

            BucketAction.create(connection, new ObjectBucketCreate(
                    prefix(bucketName),
                    namespace,
                    replicationGroup.getId(),
                    parameters
            ));

            if (parameters.containsKey(QUOTA) && parameters.get(QUOTA) != null) {
                Map<String, Integer> quota = (Map<String, Integer>) parameters.get(QUOTA);
                logger.info("Applying bucket quota on '{}' in '{}': limit {}, warn {}", prefix(bucketName), namespace, quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
                BucketQuotaAction.create(connection, namespace, prefix(bucketName), quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
            }

            if (parameters.containsKey(DEFAULT_RETENTION) && parameters.get(DEFAULT_RETENTION) != null) {
                logger.info("Applying bucket retention policy on '{}' in '{}': {}", bucketName, namespace, parameters.get(DEFAULT_RETENTION));
                BucketRetentionAction.update(connection, namespace, prefix(bucketName), (int) parameters.get(DEFAULT_RETENTION));
            }

            if (parameters.containsKey(TAGS) && parameters.get(TAGS) != null) {
                List<Map<String, String>> bucketTags = (List<Map<String, String>>) parameters.get(TAGS);
                logger.info("Applying bucket tags on '{}': {}", bucketName, bucketTags);
                BucketTagsAction.create(connection, prefix(bucketName), new BucketTagsParamAdd(namespace, bucketTags));
            }

            if (parameters.containsKey(EXPIRATION) && parameters.get(EXPIRATION) != null) {
                if ((boolean) parameters.get(FILE_ACCESSIBLE) == true) {
                    String message = "Cannot apply expiration to file accessible bucket" + bucketName;
                    logger.error(message);
                    throw new EcsManagementClientException(message);
                }
                logger.info("Granting lifecycle management bucket policy to object user '{}'", prefix(broker.getRepositoryUser()));
                grantUserLifecycleManagementPolicy(bucketName, namespace, prefix(broker.getRepositoryUser()));
                logger.info("Applying bucket expiration on '{}': {} days", bucketName, parameters.get(EXPIRATION));
                BucketExpirationAction.update(broker, prefix(bucketName), (int) parameters.get(EXPIRATION), null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to create bucket '%s': %s", bucketName, e.getMessage());
            logger.error(errorMessage, e);
            throw new ServiceBrokerException(errorMessage, e);
        }
        return parameters;
    }

    Map<String, Object> changeBucketPlan(String bucketName, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters, Map<String, Object> instanceSettings) {
        parameters = mergeParameters(broker, service, plan, parameters);

        // Validate the reclaim-policy
        validateReclaimPolicy(parameters);

        String namespace = instanceSettings != null
                ? (String) instanceSettings.getOrDefault(NAMESPACE, parameters.get(NAMESPACE))
                : (String) parameters.get(NAMESPACE);

        // keep value in service instance settings
        parameters.put(NAMESPACE, namespace);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> quota = (Map<String, Object>) parameters.getOrDefault(QUOTA, new HashMap<>());
            int limit = (int) quota.getOrDefault(QUOTA_LIMIT, -1);
            int warn = (int) quota.getOrDefault(QUOTA_WARN, -1);

            if (limit == -1 && warn == -1) {
                logger.info("Removing quota from '{}' in '{}'", prefix(bucketName), namespace);
                BucketQuotaAction.delete(connection, namespace, prefix(bucketName));

                parameters.remove(QUOTA);
            } else {
                logger.info("Setting bucket quota on '{}' in '{}': limit {}, warn {}", prefix(bucketName), namespace, quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
                BucketQuotaAction.create(connection, namespace, prefix(bucketName), limit, warn);
            }

            DefaultBucketRetention currentRetention = BucketRetentionAction.get(connection, broker.getNamespace(), prefix(bucketName));
            int newRetention = (int) parameters.getOrDefault(DEFAULT_RETENTION, 0);

            if (currentRetention.getPeriod() != newRetention) {
                logger.info("Setting bucket retention policy on '{}': {} instead of {}", prefix(bucketName), newRetention, currentRetention.getPeriod());
                BucketRetentionAction.update(connection, broker.getNamespace(), prefix(bucketName), newRetention);
                parameters.put(DEFAULT_RETENTION, newRetention);
            }

            if (parameters.containsKey(TAGS) && parameters.get(TAGS) != null) {
                changeBucketTags(bucketName, namespace, parameters);
            }

            parameters = validateAndPrepareSearchMetadata(parameters);
            List<SearchMetadata> requestedSearchMetadataList = (List<SearchMetadata>) parameters.get(SEARCH_METADATA);
            List<SearchMetadata> currentSearchMetadataList = BucketAction.get(connection, prefix(bucketName), broker.getNamespace()).getSearchMetadataList();

            if (!isEqualSearchMetadataList(requestedSearchMetadataList, currentSearchMetadataList)) {
                logger.info("Removing search metadata from '{}' in '{}'", prefix(bucketName), namespace);
                SearchMetadataAction.delete(connection, prefix(bucketName), broker.getNamespace());
            }

            if (parameters.containsKey(EXPIRATION) && parameters.get(EXPIRATION) != null) {
                changeBucketExpiration(bucketName, namespace, (int) parameters.get(EXPIRATION));
            } else {
                deleteCurrentExpirationRule(bucketName, namespace);
            }

        } catch (EcsManagementClientException | URISyntaxException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }

        return parameters;
    }

    public boolean bucketExists(String bucketName, String namespace) throws EcsManagementClientException {
        return BucketAction.exists(connection, prefix(bucketName), namespace);
    }

    private Boolean namespaceExists(String id) throws EcsManagementClientException {
        return NamespaceAction.exists(connection, id);
    }

    private boolean aclExists(String id, String namespace) throws EcsManagementClientException {
        return BucketAclAction.exists(connection, id, namespace);
    }


    UserSecretKey createUser(String id, String namespace) {
        try {
            String userId = prefix(id);

            logger.info("Creating user '{}' in namespace '{}'", userId, namespace);
            ObjectUserAction.create(connection, userId, namespace);

            logger.info("Creating secret for user '{}'", userId);
            ObjectUserSecretAction.create(connection, userId);
            return ObjectUserSecretAction.list(connection, userId).get(0);
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    void createUserMap(String username, String namespace, int uid) throws EcsManagementClientException {
        ObjectUserMapAction.create(connection, prefix(username), uid, namespace);
    }

    void deleteUserMap(String username, String namespace, String uid) throws EcsManagementClientException {
        ObjectUserMapAction.delete(connection, prefix(username), uid, namespace);
    }

    Boolean userExists(String userId, String namespace) throws ServiceBrokerException {
        try {
            return ObjectUserAction.exists(connection, prefix(userId), namespace);
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    void deleteUser(String userId, String namespace) throws EcsManagementClientException {
        if (userExists(userId, namespace)) {
            logger.info("Deleting user '{}' in namespace '{}'", userId, namespace);
            ObjectUserAction.delete(connection, prefix(userId));
        } else {
            logger.info("User {} no longer exists, assume already deleted", prefix(userId));
        }
    }

    void addUserToBucket(String bucketId, String namespace, String username) {
        try {
            addUserToBucket(bucketId, namespace, username, FULL_CONTROL);
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    void addUserToBucket(String bucketId, String namespace, String username, List<String> permissions) throws EcsManagementClientException {
        logger.info("Adding user '{}' to bucket '{}' in '{}' with {} access", prefix(username), prefix(bucketId), namespace, permissions);

        BucketAcl acl = BucketAclAction.get(connection, prefix(bucketId), namespace);

        List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
        userAcl.add(new BucketUserAcl(prefix(username), permissions));
        acl.getAcl().setUserAccessList(userAcl);

        BucketAclAction.update(connection, prefix(bucketId), acl);

        if (!getBucketFileEnabled(bucketId, namespace)) {
            BucketPolicy bucketPolicy = new BucketPolicy(
                    "2012-10-17",
                    "DefaultPCFBucketPolicy",
                    new BucketPolicyStatement("DefaultAllowTotalAccess",
                            new BucketPolicyEffect("Allow"),
                            new BucketPolicyPrincipal(prefix(username)),
                            new BucketPolicyActions(Collections.singletonList("s3:*")),
                            new BucketPolicyResource(Collections.singletonList(prefix(bucketId)))
                    )
            );
            BucketPolicyAction.update(connection, prefix(bucketId), bucketPolicy, namespace);
        }
    }

    void removeUserFromBucket(String bucket, String namespace, String username) throws EcsManagementClientException {
        if (!aclExists(prefix(bucket), namespace)) {
            logger.info("ACL {} no longer exists when removing user {}", prefix(bucket), prefix(username));
            return;
        }
        BucketAcl acl = BucketAclAction.get(connection, prefix(bucket), namespace);
        List<BucketUserAcl> newUserAcl = acl.getAcl().getUserAccessList()
                .stream().filter(a -> !a.getUser().equals(prefix(username)))
                .collect(Collectors.toList());
        acl.getAcl().setUserAccessList(newUserAcl);

        BucketAclAction.update(connection, prefix(bucket), acl);
    }

    String prefix(String string) {
        return broker.getPrefix() + string;
    }

    private void lookupObjectEndpoints() throws EcsManagementClientException {
        if (broker.getObjectEndpoint() != null) {
            try {
                URL endpointUrl = new URL(broker.getObjectEndpoint());
                objectEndpoint = broker.getObjectEndpoint();
                logger.info("Using object endpoint address from broker configuration: {}, use ssl: {}", objectEndpoint, broker.getUseSsl());
            } catch (MalformedURLException e) {
                throw new EcsManagementClientException("Malformed URL provided as object endpoint: " + broker.getObjectEndpoint());
            }
        } else {
            List<BaseUrl> baseUrlList = BaseUrlAction.list(connection);
            String urlId;

            if (baseUrlList == null || baseUrlList.isEmpty()) {
                throw new ServiceBrokerException("Cannot determine object endpoint url: base URLs list is empty, check ECS server settings");
            } else if (broker.getBaseUrl() != null) {
                urlId = baseUrlList.stream()
                        .filter(b -> broker.getBaseUrl().equals(b.getName()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceBrokerException("Configured ECS Base URL not found: " + broker.getBaseUrl()))
                        .getId();
            } else {
                Optional<BaseUrl> maybeBaseUrl = baseUrlList.stream()
                        .filter(b -> "DefaultBaseUrl".equals(b.getName()))
                        .findAny();
                if (maybeBaseUrl.isPresent()) {
                    urlId = maybeBaseUrl.get().getId();
                } else {
                    urlId = baseUrlList.get(0).getId();
                }
            }

            BaseUrlInfo baseUrl = BaseUrlAction.get(connection, urlId);
            objectEndpoint = baseUrl.getNamespaceUrl(broker.getNamespace(), broker.getUseSsl());

            logger.info("Object Endpoint address from configured base url '{}': {}", baseUrl.getName(), objectEndpoint);

            if (baseUrl.getName() != null && !baseUrl.getName().equals(broker.getBaseUrl())) {
                logger.info("Setting base url name to '{}'", baseUrl.getName());
                broker.setBaseUrl(baseUrl.getName());
            }
        }

        if (broker.getRepositoryEndpoint() == null) {
            broker.setRepositoryEndpoint(objectEndpoint);
        }
    }

    String getNamespaceURL(String namespace, Map<String, Object> requestParameters, Map<String, Object> serviceSettings) {
        Map<String, Object> parameters = broker.getSettings();
        if (requestParameters != null) {
            parameters.putAll(requestParameters);
        }
        if (serviceSettings != null) {
            // merge serviceSettings into parameters, overwriting parameter values
            // with serviceSettings, since serviceSettings are forced by administrator
            // through the catalog.
            parameters.putAll(serviceSettings);
        }

        try {
            String baseUrl = (String) parameters.getOrDefault(BASE_URL, broker.getBaseUrl());
            Boolean useSSL = (Boolean) parameters.getOrDefault(USE_SSL, broker.getUseSsl());

            return getNamespaceURL(namespace, useSSL, baseUrl);
        } catch (EcsManagementClientException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    private String getNamespaceURL(String namespace, Boolean useSSL, String baseUrl) throws EcsManagementClientException {
        if (baseUrl == null || baseUrl.isEmpty()) {
            logger.warn("Base url name is empty, returning object endpoint URL as S3 endpoint for namespace {}: {}", namespace, objectEndpoint);
            return objectEndpoint;
        }

        List<BaseUrl> baseUrlList = BaseUrlAction.list(connection);
        String urlId = baseUrlList.stream()
                .filter(b -> baseUrl != null && b != null && baseUrl.equals(b.getName()))
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("Failed to configure namespace - base URL not found: " + baseUrl))
                .getId();
        return BaseUrlAction.get(connection, urlId).getNamespaceUrl(namespace, useSSL);
    }

    private void lookupReplicationGroupID() throws EcsManagementClientException {
        DataServiceReplicationGroup rg = lookupReplicationGroup(broker.getReplicationGroup());
        logger.info("Replication group found: {} ({})", rg.getName(), rg.getId());
    }

    private void prepareRepository() throws EcsManagementClientException {
        String bucketName = broker.getRepositoryBucket();
        String namespace = broker.getNamespace();
        String userName = broker.getRepositoryUser();

        if (!bucketExists(bucketName, namespace)) {
            logger.info("Preparing repository bucket '{}'", prefix(bucketName));

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
            parameters.put(NAMESPACE, namespace);

            createBucket("repository", bucketName, service, plan, parameters);
        }

        if (!userExists(userName, namespace)) {
            logger.info("Creating user to access repository: '{}'", userName);
            UserSecretKey secretKey = createUser(userName, namespace);
            addUserToBucket(bucketName, namespace, userName);
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
        logger.info("Default Reclaim Policy: {}", ReclaimPolicy.DEFAULT_RECLAIM_POLICY);
    }

    private String getUserSecret(String userName)
            throws EcsManagementClientException {
        return ObjectUserSecretAction.list(connection, prefix(userName)).get(0)
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

    private void validateReclaimPolicy(Map<String, Object> parameters) {
        // Ensure Reclaim-Policy can be parsed
        try {
            ReclaimPolicy.getReclaimPolicy(parameters);
        } catch (IllegalArgumentException e) {
            throw new ServiceBrokerInvalidParametersException("Invalid reclaim-policy: " + ReclaimPolicy.getReclaimPolicy(parameters));
        }

        // Ensure Allowed-Reclaim-Policies can be parsed
        try {
            ReclaimPolicy.getAllowedReclaimPolicies(parameters);
        } catch (IllegalArgumentException e) {
            throw new ServiceBrokerInvalidParametersException("Invalid reclaim-policies: " + ReclaimPolicy.getReclaimPolicy(parameters));
        }

        if (!ReclaimPolicy.isPolicyAllowed(parameters)) {
            throw new ServiceBrokerInvalidParametersException("Reclaim Policy is not allowed: " + ReclaimPolicy.getReclaimPolicy(parameters));
        }
    }

    @SuppressWarnings("unchecked")
    static public Map<String, Object> validateAndPrepareSearchMetadata(Map<String, Object> parameters) {
        if (parameters.containsKey(SEARCH_METADATA)) {
            parameters = new HashMap<>(parameters);  // don't modify original map

            List<Map<String, String>> metadataList = (List<Map<String, String>>) parameters.get(SEARCH_METADATA);
            List<SearchMetadata> validatedMetadataList = new ArrayList<>();

            for (Map<String, String> metadata : metadataList) {
                String name = metadata.get(SEARCH_METADATA_NAME);
                if (name == null) {
                    throw new ServiceBrokerInvalidParametersException("Invalid search metadata: name is not provided");
                }

                String dataType = metadata.get(SEARCH_METADATA_DATATYPE);   // could be empty for system metadata
                if (dataType != null && !SearchMetadataDataType.isMetaDataType(dataType)) {
                    throw new ServiceBrokerInvalidParametersException("Invalid search metadata datatype: '" + dataType + "'");
                }

                String type = metadata.computeIfAbsent(SEARCH_METADATA_TYPE, s ->
                    SystemMetadataName.isSystemMetadata(name) ? SEARCH_METADATA_TYPE_SYSTEM : SEARCH_METADATA_TYPE_USER
                );

                switch (type) {
                    case SEARCH_METADATA_TYPE_SYSTEM:
                        SystemMetadataName systemMetadataName = SystemMetadataName.getSystemMetadataName(name);
                        if (systemMetadataName == null) {
                            throw new ServiceBrokerInvalidParametersException("Invalid system search metadata name: " + name);
                        } else {
                            String expectedDataType = systemMetadataName.getDataType().name();
                            if (dataType != null) {
                                if (!expectedDataType.equals(dataType)) {
                                    throw new ServiceBrokerInvalidParametersException(
                                            String.format("Invalid system search metadata '%s' datatype: '%s' provided instead of '%s'", name, dataType, expectedDataType)
                                    );
                                }
                            } else {
                                metadata.put(SEARCH_METADATA_DATATYPE, expectedDataType);
                            }
                        }
                        break;

                    case SEARCH_METADATA_TYPE_USER:
                        if (!name.startsWith(SEARCH_METADATA_USER_PREFIX)) {
                            metadata.put(SEARCH_METADATA_NAME, SEARCH_METADATA_USER_PREFIX + name);
                        }
                        break;

                    default:
                        throw new ServiceBrokerInvalidParametersException("Invalid type specified for search metadata: " + type);
                }
                SearchMetadata updatedMetadata = new SearchMetadata(metadata);
                validatedMetadataList.add(updatedMetadata);
            }
            parameters.put(SEARCH_METADATA, validatedMetadataList);
        }

        return parameters;
    }

    Map<String, Object> createNamespace(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters)
            throws EcsManagementClientException {
        if (namespaceExists(prefix(namespace))) {
            throw new ServiceInstanceExistsException(namespace, service.getId());
        }

        parameters = mergeParameters(broker, service, plan, parameters);

        logger.info("Creating namespace '{}' with plan '{}'({}) and params {}", prefix(namespace), plan.getName(), plan.getId(), parameters);

        DataServiceReplicationGroup replicationGroup = lookupReplicationGroup((String) parameters.get(REPLICATION_GROUP));

        NamespaceAction.create(connection, new NamespaceCreate(
                prefix(namespace),
                replicationGroup.getId(),
                parameters
        ));

        if (parameters.containsKey(QUOTA)) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> quota = (Map<String, Integer>) parameters.get(QUOTA);
            NamespaceQuotaParam quotaParam = new NamespaceQuotaParam(namespace, quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
            logger.info("Applying quota to namespace {}: block size {}, notification limit {}", namespace, quotaParam.getBlockSize(), quotaParam.getNotificationSize());
            NamespaceQuotaAction.create(connection, prefix(namespace), quotaParam);
        }

        if (parameters.containsKey(RETENTION)) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> retention = (Map<String, Integer>) parameters.get(RETENTION);
            for (Map.Entry<String, Integer> entry : retention.entrySet()) {
                logger.info("Adding retention class to namespace {}: {} = {}", namespace, entry.getKey(), entry.getValue());
                NamespaceRetentionAction.create(connection, prefix(namespace),
                        new RetentionClassCreate(entry.getKey(), entry.getValue()));
            }
        }
        return parameters;
    }

    void deleteNamespace(String namespace) throws EcsManagementClientException {
        if (namespaceExists(prefix((namespace)))) {
            NamespaceAction.delete(connection, prefix(namespace));
        } else {
            logger.info("Namespace {} no longer exists, assume already deleted", prefix(namespace));
        }
    }

    /**
     * Handle extra steps after a bucket wipe has completed.
     * <p>
     * Throwing an exception here will throw an exception in the CompletableFuture pipeline to signify the operation failed
     */
    private void bucketWipeCompleted(BucketWipeResult result, String id, String namespace) {
        // Wipe Failed, mark as error
        if (!result.getErrors().isEmpty()) {
            logger.warn("Bucket wipe FAILED, deleted {} objects. Leaving bucket {}", result.getDeletedObjects(), prefix(id));
            result.getErrors().forEach(error -> logger.warn("BucketWipe {} error: {}", prefix(id), error));
            throw new RuntimeException("BucketWipe Failed with " + result.getErrors().size() + " errors: " + result.getErrors().get(0));
        }

        // Wipe Succeeded, Attempt Bucket Delete
        try {
            logger.info("Bucket wipe succeeded, deleted {} objects, Deleting bucket {}", result.getDeletedObjects(), prefix(id));
            deleteBucket(id, namespace);
        } catch (Exception e) {
            logger.error("Error deleting bucket " + prefix(id), e);
            throw new RuntimeException("Error Deleting Bucket " + prefix(id) + " " + e.getMessage());
        }
    }

    Map<String, Object> changeNamespacePlan(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException {
        parameters = mergeParameters(broker, service, plan, parameters);

        logger.info("Changing namespace '{}' plan to '{}'({}) with parameters {}", namespace, plan.getName(), plan.getId(), parameters);

        NamespaceAction.update(connection, prefix(namespace),
                new NamespaceUpdate(parameters));

        if (parameters.containsKey(RETENTION)) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> retention = (Map<String, Integer>) parameters.get(RETENTION);

            for (Map.Entry<String, Integer> entry : retention.entrySet()) {
                if (NamespaceRetentionAction.exists(connection, namespace, entry.getKey())) {
                    if (-1 == entry.getValue()) {
                        logger.info("Removing retention action attribute from namespace '{}'", prefix(namespace));
                        NamespaceRetentionAction.delete(connection, prefix(namespace), entry.getKey());
                        parameters.remove(RETENTION);
                    } else {
                        logger.info("Updating retention action attribute on namespace '{}' to '{}'", prefix(namespace), entry.getValue());
                        NamespaceRetentionAction.update(connection, prefix(namespace), entry.getKey(), new RetentionClassUpdate(entry.getValue()));
                    }
                } else {
                    logger.info("Setting retention action attribute on namespace '{}' to '{}'", prefix(namespace), entry.getValue());
                    NamespaceRetentionAction.create(connection, prefix(namespace), new RetentionClassCreate(entry.getKey(), entry.getValue()));
                }
            }
        }
        return parameters;
    }

    ServiceDefinitionProxy lookupServiceDefinition(String serviceDefinitionId) throws ServiceBrokerException {
        ServiceDefinitionProxy service = catalog.findServiceDefinition(serviceDefinitionId);
        if (service == null) {
            throw new ServiceInstanceDoesNotExistException(serviceDefinitionId);
        }
        return service;
    }

    String addExportToBucket(String bucket, String namespace, String relativeExportPath) throws EcsManagementClientException {
        if (relativeExportPath == null)
            relativeExportPath = "";
        String absoluteExportPath = "/" + namespace + "/" + prefix(bucket) + "/" + relativeExportPath;
        List<NFSExport> exports = NFSExportAction.list(connection, absoluteExportPath);
        if (exports == null) {
            logger.info("Creating NFS export path '{}'", absoluteExportPath);
            NFSExportAction.create(connection, absoluteExportPath);
        } else {
            logger.info("Skipping NFS export create - non-empty exports list found for path '{}'", absoluteExportPath);
        }
        return absoluteExportPath;
    }


    public DataServiceReplicationGroup lookupReplicationGroup(String replicationGroup) throws EcsManagementClientException {
        return ReplicationGroupAction.list(connection).stream()
                .filter(r -> replicationGroup != null && r != null
                        && (replicationGroup.equals(r.getName()) || replicationGroup.equals(r.getId()))
                )
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("ECS replication group not found: " + replicationGroup));
    }

    void grantUserLifecycleManagementPolicy(String bucket, String namespace, String username) {
        List<String> actions = new ArrayList<>();
        actions.add(S3_ACTION_PUT_LC_CONFIG);
        actions.add(S3_ACTION_GET_LC_CONFIG);
        actions.add(S3_ACTION_GET_BUCKET_POLICY);

        String statementId = "Grant permission for lifecycle configuration to " + username;
        BucketPolicy policy = new BucketPolicy(
                BUCKET_POLICY_VERSION,
                "LifecycleManagementBucketPolicy",
                new BucketPolicyStatement(statementId,
                        new BucketPolicyEffect("Allow"),
                        new BucketPolicyPrincipal(username),
                        new BucketPolicyActions(actions),
                        new BucketPolicyResource(Collections.singletonList(prefix(bucket)))
                ));
        BucketPolicyAction.update(connection, prefix(bucket), policy, namespace);
    }

    /**
     * Merge request bucket tags with plan and service provided tags
     * <p>
     * Request bucket tags are overwritten with plan and service ones,
     * while bucket tags provided in plan description are overwritten by service tags
     * since service settings are forced by administrator through the catalog
     */
    static List<Map<String, String>> mergeBucketTags(ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters) {
        List<Map<String, String>> serviceTags = (List<Map<String, String>>)service.getServiceSettings().get(TAGS);
        List<Map<String, String>> planTags = (List<Map<String, String>>)plan.getServiceSettings().get(TAGS);
        List<Map<String, String>> requestedTags = (List<Map<String, String>>)requestParameters.get(TAGS);
        List<Map<String, String>> unmatchedTags;

        if (planTags != null && serviceTags != null) {
            unmatchedTags = new ArrayList<>(planTags);

            for (Map<String, String> planTag: planTags) {
                for (Map<String, String> serviceTag: serviceTags) {
                    if (planTag.get(KEY).equals(serviceTag.get(KEY))) {
                        unmatchedTags.remove(planTag);
                    }
                }
            }

            serviceTags = Stream.concat(serviceTags.stream(), unmatchedTags.stream()).collect(Collectors.toList());
        } else if (serviceTags == null && planTags != null) {
            serviceTags = new ArrayList<>(planTags);
        }

        if (requestedTags != null && serviceTags != null) {
            unmatchedTags = new ArrayList<>(requestedTags);

            for (Map<String, String> requestedTag: requestedTags) {
                for (Map<String, String> serviceTag: serviceTags) {
                    if (requestedTag.get(KEY).equals(serviceTag.get(KEY))) {
                        unmatchedTags.remove(requestedTag);
                        break;
                    }
                }
            }

            serviceTags = Stream.concat(serviceTags.stream(), unmatchedTags.stream()).collect(Collectors.toList());
        } else if (serviceTags == null && requestedTags != null) {
            serviceTags = new ArrayList<>(requestedTags);
        }

        return serviceTags;
    }

    /**
     * Merge request search metadata with service provided metadata
     * <p>
     * Request bucket tags are overwritten with service ones
     * since service settings are forced by administrator through the catalog
     */
    static List<Map<String, String>> mergeSearchMetadata(ServiceDefinitionProxy service, Map<String, Object> requestParameters) {
        List<Map<String, String>> serviceMetadata = (List<Map<String, String>>) service.getServiceSettings().get(SEARCH_METADATA);
        List<Map<String, String>> requestedMetadata = (List<Map<String, String>>)requestParameters.get(SEARCH_METADATA);

        if (serviceMetadata == null) {
            return requestedMetadata;
        } else if (requestedMetadata == null) {
            return serviceMetadata;
        } else {
            List<Map<String, String>> unmatchedMetadata = new ArrayList<>(requestedMetadata);
            for (Map<String, String> requestedMetadatum: requestedMetadata) {
                for (Map<String, String> serviceMetadatum: serviceMetadata) {
                    if (requestedMetadatum.get(SEARCH_METADATA_NAME).equals(serviceMetadatum.get(SEARCH_METADATA_NAME))) {
                        unmatchedMetadata.remove(requestedMetadatum);
                        break;
                    }
                }
            }
            return Stream.concat(serviceMetadata.stream(), unmatchedMetadata.stream()).collect(Collectors.toList());
        }
    }

    /**
     * Merge request additional parameters with with broker, plan and service settings
     * <p>
     * Broker settings (replication group, namespace and base url name) are overwritten with request parameters;
     * request parameter values are overwritten with plan and service settings,
     * since service settings are forced by administrator through the catalog
     */
    static Map<String, Object> mergeParameters(BrokerConfig brokerConfig, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters) {
        Map<String, Object> ret = new HashMap<>(brokerConfig.getSettings());

        if (requestParameters != null) ret.putAll(requestParameters);

        ret.putAll(plan.getServiceSettings());

        ret.putAll(service.getServiceSettings());

        List<Map<String, String>> tags = mergeBucketTags(service, plan, requestParameters);

        if (tags != null) {
            ret.put(TAGS, tags);
        }

        List<Map<String, String>> searchMetadata = mergeSearchMetadata(service, requestParameters);

        if (searchMetadata != null) {
            ret.put(SEARCH_METADATA, searchMetadata);
        }

        return ret;
    }

    Map<String, Object> mergeParameters(ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters) {
        return mergeParameters(broker, service, plan, requestParameters);
    }

    public String getDefaultNamespace() {
        return broker.getNamespace();
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> changeBucketTags(String bucketName, String namespace, Map<String, Object> parameters) {
        List<BucketTag> requestedTags = new BucketTagSetRootElement((List<Map<String, String>>) parameters.get(TAGS)).getTagSet();
        List<BucketTag> currentTags = BucketAction.get(connection, prefix(bucketName), namespace).getTagSet();

        List<BucketTag> createTags = new ArrayList<>();
        List<BucketTag> updateTags = new ArrayList<>();
        List<BucketTag> paramsTags = new ArrayList<>();

        do {
            BucketTag requestedTag = requestedTags.get(0);
            boolean isNew = true;
            for (BucketTag currentTag : currentTags) {
                if (requestedTag.getKey().equals(currentTag.getKey())) {
                    if (!requestedTag.getValue().equals(currentTag.getValue())) {
                        updateTags.add(requestedTag);
                    }
                    isNew = false;
                    currentTags.remove(currentTag);
                    break;
                }
            }
            paramsTags.add(requestedTag);
            if (isNew) {
                createTags.add(requestedTag);
            }
            requestedTags.remove(requestedTag);
        } while (!requestedTags.isEmpty());

        paramsTags.addAll(currentTags);

        if (!createTags.isEmpty()) {
            BucketTagSetRootElement createTagSet = new BucketTagSetRootElement();
            createTagSet.setTagSet(createTags);
            logger.info("Setting new bucket tags on '{}': {}", prefix(bucketName), createTagSet);
            BucketTagsAction.create(connection, prefix(bucketName), new BucketTagsParamAdd(namespace, createTagSet.getTagSetAsListOfTags()));
        }

        if (!updateTags.isEmpty()) {
            BucketTagSetRootElement updateTagSet = new BucketTagSetRootElement();
            updateTagSet.setTagSet(updateTags);
            logger.info("Setting new values of existing bucket tags on '{}': {}", prefix(bucketName), updateTagSet);
            BucketTagsAction.update(connection, prefix(bucketName), new BucketTagsParamUpdate(namespace, updateTagSet.getTagSetAsListOfTags()));
        }

        BucketTagSetRootElement paramsTagSet = new BucketTagSetRootElement();
        paramsTagSet.setTagSet(paramsTags);

        if (updateTags.size() + createTags.size() != 0) {
            logger.info("Full set of bucket tags on '{}' in '{}' is {}", prefix(bucketName), namespace, paramsTagSet);
        }

        parameters.put(TAGS, paramsTagSet.getTagSetAsListOfTags());

        return parameters;
    }

    private void provideUserWithLifecycleManagementPolicy(String bucketName, String namespace, String user) {
        List<String> actions = new ArrayList<>();
        try {
            logger.debug("Checking lifecycle management bucket policy to object user '{}'", prefix(broker.getRepositoryUser()));
            BucketPolicyStatement bucketPolicyStatement = BucketPolicyAction.get(connection, prefix(bucketName), namespace).getBucketPolicyStatement();
            actions = bucketPolicyStatement.getBucketPolicyAction();
        } catch (RuntimeException e) {
            logger.debug("Object user '{}' does not have reading bucket policy permissions", prefix(broker.getRepositoryUser()));
        }

        if(!actions.contains(S3_ACTION_GET_LC_CONFIG) || !actions.contains(S3_ACTION_PUT_LC_CONFIG)) {
            logger.info("Granting lifecycle management bucket policy to object user '{}'", prefix(broker.getRepositoryUser()));
            grantUserLifecycleManagementPolicy(bucketName, namespace, user);
        }
    }

    void changeBucketExpiration(String bucketName, String namespace, int days) throws URISyntaxException {
        provideUserWithLifecycleManagementPolicy(bucketName, namespace, prefix(broker.getRepositoryUser()));
        LifecycleConfiguration configuration = BucketExpirationAction.get(broker, prefix(bucketName));

        if (configuration == null || configuration.getRules() == null) {
            logger.info("Applying bucket expiration on '{}': {} days", bucketName, days);
            BucketExpirationAction.update(broker, prefix(bucketName), days, null);
        } else {
            List<LifecycleRule> rules = new ArrayList<>(configuration.getRules());
            for (LifecycleRule rule: rules) {
                if (rule.getStatus() == LifecycleRule.Status.Enabled && rule.getId().startsWith(BucketExpirationAction.RULE_PREFIX)) {
                    if (rule.getExpirationDays() != days) {
                        logger.info("Changing bucket expiration on '{}': {} days instead of {} days", bucketName, days, rule.getExpirationDays());
                        rules.remove(rule);
                        BucketExpirationAction.update(broker, prefix(bucketName), days, rules);
                    }
                    return;
                }
            }
            logger.info("Applying bucket expiration on '{}': {} days", bucketName, days);
            BucketExpirationAction.update(broker, prefix(bucketName), days, rules);
        }
    }

    void deleteCurrentExpirationRule(String bucketName, String namespace) throws URISyntaxException {
        provideUserWithLifecycleManagementPolicy(bucketName, namespace, prefix(broker.getRepositoryUser()));
        LifecycleConfiguration configuration = BucketExpirationAction.get(broker, prefix(bucketName));

        if (configuration != null && configuration.getRules() != null) {
            List<LifecycleRule> rules = new ArrayList<>(configuration.getRules());
            for (LifecycleRule rule: rules) {
                if (rule.getStatus() == LifecycleRule.Status.Enabled && rule.getId().startsWith(BucketExpirationAction.RULE_PREFIX)) {
                    logger.info("Removing bucket expiration {} days on '{}' bucket ", rule.getExpirationDays(), bucketName);
                    rules.remove(rule);
                    BucketExpirationAction.delete(broker, prefix(bucketName), rule.getId(), rules);
                    return;
                }
            }
        }
    }

    static boolean isEqualSearchMetadataList(List<SearchMetadata> list1, List<SearchMetadata> list2) {
        if (list1 == null && list2 == null) {
            return true;
        } else if (list1 == null || list2 == null) {
            return false;
        } else {
            if (list1.size() == list2.size()) {
                list1.sort(SearchMetadata::compareTo);
                list2.sort(SearchMetadata::compareTo);
                for (int i = 0; i < list1.size(); i++) {
                    SearchMetadata metadata1 = list1.get(i);
                    SearchMetadata metadata2 = list2.get(i);
                    if (!metadata1.getName().equalsIgnoreCase(metadata2.getName()) ||
                        !metadata1.getType().equalsIgnoreCase(metadata2.getType()) ||
                        !metadata1.getDatatype().equalsIgnoreCase(metadata2.getDatatype())) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public Map<String, Object> getBrokerConfig() {
        return broker.getSettings();
    }
}
