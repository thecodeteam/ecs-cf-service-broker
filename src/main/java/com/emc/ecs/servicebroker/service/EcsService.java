package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.actions.*;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.*;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.servicebroker.service.s3.BucketExpirationAction;
import com.emc.ecs.servicebroker.service.utils.TagValuesHandler;
import com.emc.ecs.tool.BucketWipeOperations;
import com.emc.ecs.tool.BucketWipeResult;
import com.emc.object.s3.bean.LifecycleConfiguration;
import com.emc.object.s3.bean.LifecycleRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECTSCALE;
import static com.emc.ecs.servicebroker.model.Constants.*;

@Service
public class EcsService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(EcsService.class);

    @Autowired
    @Qualifier("managementAPI")
    protected ManagementAPIConnection connection;

    @Autowired
    protected BrokerConfig broker;

    @Autowired
    protected CatalogConfig catalog;

    @Autowired
    protected BucketWipeFactory bucketWipeFactory;

    protected BucketWipeOperations bucketWipe;

    private String objectEndpoint;

    @Override
    public String getObjectEndpoint() {
        return objectEndpoint;
    }

    @Override
    public Map<String, Object> getBrokerConfig() {
        return broker.getSettings();
    }

    @Override
    public String getNfsMountHost() {
        return broker.getNfsMountHost();
    }

    @Override
    public String getDefaultNamespace() {
        return broker.getNamespace();
    }

    @PostConstruct
    void initialize() {
        if (broker.isConfigValidationMode()) {
            logger.info("Skipping ECS service initialization - working in validation mode");
            return;
        }

        if (!OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            // API type is ECS, initializing service

            logger.info("Initializing ECS service with management endpoint {}, base url {}", broker.getManagementEndpoint(), broker.getBaseUrl());

            try {
                lookupObjectEndpoints();
                lookupRepositoryNamespace();
                lookupReplicationGroupID();
                prepareDefaultReclaimPolicy();
                prepareRepository();
                getS3RepositorySecret();
                prepareBucketWipe();
            } catch (EcsManagementClientException | URISyntaxException e) {
                logger.error("Failed to initialize ECS service: {}", e.getMessage());
                throw new ServiceBrokerException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void deleteBucket(String bucketName, String namespace) {
        if (namespace == null) {
            // buckets created prior to ver2.1 doesnt have namespace in their settings - using old default
            namespace = broker.getNamespace();
        }
        String prefixedBucketName = prefix(bucketName);
        try {
            if (namespaceExists(namespace) && bucketExists(prefixedBucketName, namespace)) {
                logger.info("Deleting bucket '{}' from namespace '{}'", prefixedBucketName, namespace);
                BucketAction.delete(connection, prefixedBucketName, namespace);
            } else {
                logger.info("Bucket '{}' no longer exists in '{}', assume already deleted", prefixedBucketName, namespace);
            }
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture wipeAndDeleteBucket(String bucketName, String namespace) {
        if (namespace == null) {
            namespace = broker.getNamespace();
        }
        try {
            if (!namespaceExists(namespace) || !bucketExists(prefix(bucketName), namespace)) {
                logger.info("Bucket '{}' no longer exists in '{}', assume already deleted", bucketName, namespace);
                return null;
            }

            addUserToBucket(bucketName, namespace, broker.getRepositoryUser());

            logger.info("Started wipe of bucket '{}' in namespace '{}'", bucketName, namespace);
            BucketWipeResult result = bucketWipeFactory.newBucketWipeResult();
            bucketWipe.deleteAllObjects(prefix(bucketName), "", result);

            String ns = namespace;

            return result.getCompletedFuture().thenRun(() -> bucketWipeCompleted(result, bucketName, ns));
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean getBucketFileEnabled(String bucketName, String namespace) throws EcsManagementClientException {
        ObjectBucketInfo b = BucketAction.get(connection, prefix(bucketName), namespace);
        return b.getFsAccessEnabled();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> createBucket(String serviceInstanceId, String bucketName, ServiceDefinitionProxy serviceDefinition,
                                            PlanProxy plan, Map<String, Object> parameters) {

        boolean bucketCreated = false;
        try {
            parameters = mergeParameters(broker, serviceDefinition, plan, parameters);
            parameters = validateAndPrepareSearchMetadata(parameters);

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

            String prefixedBucketName = prefix(bucketName);

            logger.info("Creating bucket '{}' with service '{}' plan '{}'({}) and params {}", prefixedBucketName, serviceDefinition.getName(), plan.getName(), plan.getId(), parameters);

            String namespace = (String) parameters.get(NAMESPACE);

            if (bucketExists(prefixedBucketName, namespace)) {
                logger.info("Bucket '{}' already exists in namespace '{}'", prefixedBucketName, namespace);
                throw new ServiceInstanceExistsException(serviceInstanceId, serviceDefinition.getId());
            }

            DataServiceReplicationGroup replicationGroup = lookupReplicationGroup((String) parameters.get(REPLICATION_GROUP));

            BucketAction.create(connection, new ObjectBucketCreate(
                    prefixedBucketName,
                    namespace,
                    replicationGroup.getId(),
                    parameters
            ));

            bucketCreated = true;

            if (parameters.containsKey(QUOTA) && parameters.get(QUOTA) != null) {
                Map<String, Integer> quota = (Map<String, Integer>) parameters.get(QUOTA);
                logger.info("Applying bucket quota on '{}' in '{}': limit {}, warn {}", prefixedBucketName, namespace, quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
                BucketQuotaAction.create(connection, namespace, prefixedBucketName, quota.get(QUOTA_LIMIT), quota.get(QUOTA_WARN));
            }

            if (parameters.containsKey(DEFAULT_RETENTION) && parameters.get(DEFAULT_RETENTION) != null) {
                logger.info("Applying bucket retention policy on '{}' in '{}': {}", prefixedBucketName, namespace, parameters.get(DEFAULT_RETENTION));
                BucketRetentionAction.update(connection, namespace, prefixedBucketName, (int) parameters.get(DEFAULT_RETENTION));
            }

            if (parameters.containsKey(TAGS) && parameters.get(TAGS) != null) {
                List<Map<String, String>> bucketTags = (List<Map<String, String>>) parameters.get(TAGS);
                TagValuesHandler.substituteContextValues(bucketTags, parameters);
                logger.info("Applying bucket tags on '{}': {}", prefixedBucketName, bucketTags);
                BucketTagsAction.create(connection, prefixedBucketName, new BucketTagsParamAdd(namespace, bucketTags));
            }

            if (parameters.containsKey(EXPIRATION) && parameters.get(EXPIRATION) != null) {
                grantUserLifecycleManagementPolicy(prefixedBucketName, namespace, broker.getRepositoryUser());
                logger.info("Applying bucket expiration on '{}': {} days", bucketName, parameters.get(EXPIRATION));
                BucketExpirationAction.update(broker, namespace, prefixedBucketName, (int) parameters.get(EXPIRATION), null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to create bucket '%s': %s", bucketName, e.getMessage());
            logger.error(errorMessage, e);

            if (bucketCreated) {
                logger.info("Rolling back operation: deleting bucket '{}'", prefix(bucketName));
                BucketAction.delete(connection, prefix(bucketName), (String) parameters.get(NAMESPACE));
            }

            throw new ServiceBrokerException(errorMessage, e);
        }
        return parameters;
    }

    @Override
    public Map<String, Object> changeBucketPlan(String bucketName, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters, Map<String, Object> instanceSettings) {
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

            DefaultBucketRetention currentRetention = BucketRetentionAction.get(connection, namespace, prefix(bucketName));
            int newRetention = (int) parameters.getOrDefault(DEFAULT_RETENTION, 0);

            if (currentRetention.getPeriod() != newRetention) {
                logger.info("Setting bucket retention policy on '{}': {} instead of {}", prefix(bucketName), newRetention, currentRetention.getPeriod());
                BucketRetentionAction.update(connection, namespace, prefix(bucketName), newRetention);
                parameters.put(DEFAULT_RETENTION, newRetention);
            }

            if (parameters.containsKey(TAGS) && parameters.get(TAGS) != null) {
                changeBucketTags(bucketName, namespace, parameters);
            }

            parameters = validateAndPrepareSearchMetadata(parameters);
            ObjectBucketInfo bucketInfo = BucketAction.get(connection, prefix(bucketName), namespace);
            List<SearchMetadata> requestedSearchMetadataList = (List<SearchMetadata>) parameters.get(SEARCH_METADATA);
            List<SearchMetadata> currentSearchMetadataList = bucketInfo.getSearchMetadataList();

            if (!isEqualSearchMetadataList(requestedSearchMetadataList, currentSearchMetadataList)) {
                logger.info("Removing search metadata from '{}' in '{}'", prefix(bucketName), namespace);
                SearchMetadataAction.delete(connection, prefix(bucketName), namespace);
            }

            if (parameters.containsKey(EXPIRATION) && parameters.get(EXPIRATION) != null) {
                changeBucketExpiration(bucketName, namespace, (int) parameters.get(EXPIRATION));
            } else {
                deleteCurrentExpirationRule(bucketName, namespace);
            }

            Boolean accessDuringOutage = (Boolean) parameters.get(ACCESS_DURING_OUTAGE);
            if (accessDuringOutage != null && !Objects.equals(accessDuringOutage, bucketInfo.getIsStaleAllowed())) {
                logger.info("Changing ADO for '{}' in '{}' to '{}'", prefix(bucketName), namespace, accessDuringOutage);
                BucketAdoAction.update(connection, namespace, prefix(bucketName), accessDuringOutage);
            }

        } catch (EcsManagementClientException | URISyntaxException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }

        return parameters;
    }

    public boolean bucketExists(String bucketName, String namespace) throws EcsManagementClientException {
        return BucketAction.exists(connection, bucketName, namespace);
    }

    public boolean namespaceExists(String namespace) throws EcsManagementClientException {
        return NamespaceAction.exists(connection, namespace);
    }

    public boolean aclExists(String bucketName, String namespace) throws EcsManagementClientException {
        return BucketAclAction.exists(connection, bucketName, namespace);
    }


    @Override
    public UserSecretKey createUser(String id, String namespace) {
        try {
            String userId = prefix(id);

            logger.info("Creating user '{}' in namespace '{}'", userId, namespace);
            ObjectUserAction.create(connection, userId, namespace);

            logger.info("Creating secret for user '{}'", userId);
            ObjectUserSecretAction.create(connection, userId);

            UserSecretKey userSecretKey = ObjectUserSecretAction.list(connection, userId).get(0);

            return userSecretKey;
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void createUserMap(String username, String namespace, int uid) throws EcsManagementClientException {
        ObjectUserMapAction.create(connection, prefix(username), uid, namespace);
    }

    @Override
    public void deleteUserMap(String username, String namespace, String uid) throws EcsManagementClientException {
        ObjectUserMapAction.delete(connection, prefix(username), uid, namespace);
    }

    @Override
    public Boolean userExists(String userId, String namespace) throws ServiceBrokerException {
        return objectUserExists(prefix(userId), namespace);
    }

    // checks if raw object user exists (no prefixing)
    private Boolean objectUserExists(String objectUser, String namespace) throws ServiceBrokerException {
        try {
            return ObjectUserAction.exists(connection, objectUser, namespace);
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String userId, String namespace) throws EcsManagementClientException {
        try {
            if (userExists(userId, namespace)) {
                logger.info("Deleting user '{}' in namespace '{}'", userId, namespace);
                ObjectUserAction.delete(connection, prefix(userId));
            } else {
                logger.info("User {} no longer exists, assume already deleted", prefix(userId));
            }
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void addUserToBucket(String bucketId, String namespace, String username) {
        try {
            addUserToBucket(bucketId, namespace, username, FULL_CONTROL);
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void addUserToBucket(String bucketId, String namespace, String username, List<String> permissions) throws EcsManagementClientException {
        logger.info("Adding user '{}' to bucket '{}' in '{}' with {} access", prefix(username), prefix(bucketId), namespace, permissions);

        BucketAcl acl = BucketAclAction.get(connection, prefix(bucketId), namespace);

        List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();

        // Idempotency: check if user is already added
        if (userAcl.stream().anyMatch(userPerm -> Objects.equals(prefix(username), userPerm.getUser()) && Objects.equals(permissions, userPerm.getPermissions()))) {
            logger.info("Found existing permissions for user '{}' on bucket '{}' in '{}'", prefix(username), prefix(bucketId), namespace);
        } else {
            userAcl.add(new BucketUserAcl(prefix(username), permissions));
            acl.getAcl().setUserAccessList(userAcl);

            BucketAclAction.update(connection, prefix(bucketId), acl);
        }

        if (!getBucketFileEnabled(bucketId, namespace)) {
            String statementId = getPolicyStatementId(username);
            BucketPolicyStatement statement = new BucketPolicyStatement(statementId,
                    new BucketPolicyEffect("Allow"),
                    new BucketPolicyPrincipal(prefix(username)),
                    new BucketPolicyActions(Collections.singletonList("s3:*")),
                    new BucketPolicyResource(Collections.singletonList(prefix(bucketId))));

            // Idempotency: check if permission already exists
            BucketPolicy bucketPolicy;
            // first, check if any policy exists
            if (BucketPolicyAction.exists(connection, prefix(bucketId), namespace)) {
                bucketPolicy = BucketPolicyAction.get(connection, prefix(bucketId), namespace);
            } else {
                bucketPolicy = new BucketPolicy(BUCKET_POLICY_VERSION, "DefaultPCFBucketPolicy", new ArrayList<>());
            }
            // then, only update the policy if it was modified (statement was missing), which should be true if we created with an empty list above
            if (addPolicyStatement(bucketPolicy, statement)) {
                checkPolicyForMissingUsers(prefix(bucketId), bucketPolicy, namespace);
                logger.info("Updating bucket policy {} after adding statement for user {}", prefix(bucketId), prefix(username));
                BucketPolicyAction.update(connection, prefix(bucketId), bucketPolicy, namespace);
            } else {
                logger.info("Bucket policy {} already contains a statement for user {}", prefix(bucketId), prefix(username));
            }
        }
    }

    @Override
    public void removeUserFromBucket(String bucket, String namespace, String username) throws EcsManagementClientException {
        if (aclExists(prefix(bucket), namespace)) {
            BucketAcl acl = BucketAclAction.get(connection, prefix(bucket), namespace);

            List<BucketUserAcl> newUserAcl = acl.getAcl().getUserAccessList()
                    .stream().filter(a -> !a.getUser().equals(prefix(username)))
                    .collect(Collectors.toList());
            acl.getAcl().setUserAccessList(newUserAcl);

            logger.info("Updating ACL {} after removing user {}", prefix(bucket), prefix(username));
            BucketAclAction.update(connection, prefix(bucket), acl);
        } else {
            logger.info("ACL {} no longer exists when removing user {}", prefix(bucket), prefix(username));
        }

        if (BucketPolicyAction.exists(connection, prefix(bucket), namespace)) {
            // remove the policy statement for this user
            BucketPolicy policy = BucketPolicyAction.get(connection, prefix(bucket), namespace);
            if (removePolicyStatementForPrincipal(policy, prefix(username))) {
                checkPolicyForMissingUsers(prefix(bucket), policy, namespace);
                logger.info("Updating bucket policy {} after removing user {}", prefix(bucket), prefix(username));
                if (policy.getBucketPolicyStatements().size() == 0) {
                    logger.info("There are no remaining active statements in the policy for bucket {}, so it will be deleted", prefix(bucket));
                    BucketPolicyAction.remove(connection, prefix(bucket), namespace);
                } else {
                    BucketPolicyAction.update(connection, prefix(bucket), policy, namespace);
                }
            } else {
                logger.info("Bucket policy {} no longer contains statement for user {}", prefix(bucket), prefix(username));
            }
        } else {
            logger.info("Bucket policy {} no longer exists when removing user {}", prefix(bucket), prefix(username));
        }
    }

    @Override
    public String prefix(String string) {
        if (string.startsWith(broker.getPrefix())) {
            logger.warn("String already prefixed: {}", string);
        }
        return broker.getPrefix() + string;
    }

    static String getPolicyStatementId(String username) {
        return "ecs-cf-broker-sid-" + username;
    }

    /**
     * Returns the statement in the provided policy that matches the provided principal (user)
     */
    static BucketPolicyStatement getPolicyStatementByPrincipal(BucketPolicy policy, String principal) {
        return policy.getBucketPolicyStatements().stream()
                .filter(statement -> principal.equals(statement.getPrincipal()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes the statement in the provided policy that matches the provided principal (user), if it exists.
     *
     * @return true if the policy was modified, false otherwise
     */
    static boolean removePolicyStatementForPrincipal(BucketPolicy policy, String principal) {
        BucketPolicyStatement statement = getPolicyStatementByPrincipal(policy, principal);
        if (statement != null) {
            policy.getBucketPolicyStatements().remove(statement);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds the provided statement to the provided policy, unless an equivalent statement already exists on the policy.
     * An equivalent statement exists if any existing statement in the policy has the same principal (user).
     *
     * @return true if the policy was modified, false otherwise
     */
    static boolean addPolicyStatement(BucketPolicy policy, BucketPolicyStatement statement) {
        if (getPolicyStatementByPrincipal(policy, statement.getPrincipal()) == null) {
            policy.getBucketPolicyStatements().add(statement);
            return true;
        } else {
            return false;
        }
    }

    void checkPolicyForMissingUsers(String bucket, BucketPolicy bucketPolicy, String namespace) {
        // if any other user/binding in the policy is somehow missing, we cannot update the policy, so check for missing users here
        for (Iterator<BucketPolicyStatement> chkStmtI = bucketPolicy.getBucketPolicyStatements().iterator(); chkStmtI.hasNext();) {
            String principal = chkStmtI.next().getPrincipal();
            if (!objectUserExists(principal, namespace)) {
                logger.warn("Policy for bucket {} includes user/binding {} that does not exist - please check the service bindings and policy for this bucket to make sure they are in sync or applications may lose access",
                        bucket, principal);
                chkStmtI.remove();
            }
        }
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

    @Override
    public String getNamespaceURL(String namespace, Map<String, Object> requestParameters, Map<String, Object> serviceSettings) {
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

    @Override
    public String getNamespaceURL(String namespace, Boolean useSSL, String baseUrl) throws EcsManagementClientException {
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

    private void lookupRepositoryNamespace() throws EcsManagementClientException {
        String namespace = broker.getNamespace();
        if (namespace != null) {
            logger.info("Repository namespace '{}'", broker.getNamespace());
            if (!NamespaceAction.exists(this.connection, namespace)) {
                logger.warn("Repository namespace not found: {}", namespace);
                //throw new ServiceBrokerException("Namespace not found: " + namespace);
            }
        } else {
            logger.warn("Repository namespace not configured");
        }
    }

    private void lookupReplicationGroupID() throws EcsManagementClientException {
        DataServiceReplicationGroup rg = lookupReplicationGroup(broker.getReplicationGroup());
        logger.info("Replication group found: {} ({})", rg.getName(), rg.getId());
    }

    private void prepareRepository() throws EcsManagementClientException {
        String bucketName = broker.getRepositoryBucket();
        String namespace = broker.getNamespace();
        String userName = broker.getRepositoryUser();

        prepareRepositoryBucket(bucketName, namespace);
        prepareRepositoryUser(bucketName, namespace, userName);
    }

    protected void prepareRepositoryBucket(String bucketName, String namespace) {
        String prefixedBucketName = prefix(bucketName);
        if (!bucketExists(prefixedBucketName, namespace)) {
            logger.info("Preparing repository bucket '{}'", prefixedBucketName);

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

    protected void prepareRepositoryUser(String bucketName, String namespace, String userName) {
        if (!userExists(userName, namespace)) {
            logger.info("Creating user to access repository: '{}'", userName);
            createUser(userName, namespace);
            addUserToBucket(bucketName, namespace, userName);
        }
    }

    private void getS3RepositorySecret() {
        String bucketName = broker.getRepositoryBucket();
        String namespace = broker.getNamespace();
        String userName = broker.getRepositoryUser();

        logger.info("Obtaining user secret key for repository bucket access: user '{}', bucket '{}', namespace '{}'", userName, bucketName, namespace);

        String userSecret = getUserSecret(userName);
        if (userSecret == null || userSecret.length() == 0) {
            logger.info("User secret not found, using empty value.");
        }

        broker.setRepositorySecret(userSecret);
    }

    protected void prepareBucketWipe() throws URISyntaxException {
        bucketWipe = bucketWipeFactory.getBucketWipe(broker);
    }

    private void prepareDefaultReclaimPolicy() {
        String defaultReclaimPolicy = broker.getDefaultReclaimPolicy();
        if (defaultReclaimPolicy != null) {
            ReclaimPolicy.DEFAULT_RECLAIM_POLICY = ReclaimPolicy.valueOf(defaultReclaimPolicy);
        }
        logger.info("Default Reclaim Policy: {}", ReclaimPolicy.DEFAULT_RECLAIM_POLICY);
    }

    private String getUserSecret(String userName) throws EcsManagementClientException {
        List<UserSecretKey> keys = ObjectUserSecretAction.list(connection, prefix(userName));
        if (keys == null || keys.size() == 0) {
            throw new EcsManagementClientException("Cannot find user '" + prefix(userName) + "' secret - empty list returned");
        }
        if (keys.size() > 1) {
            logger.warn("Found " + keys.size() + " secret keys for user '" + prefix(userName) + "', returning first");
        }
        return keys.get(0).getSecretKey();
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

    @Override
    public Map<String, Object> createNamespace(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters)
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

    @Override
    public void deleteNamespace(String namespace) throws EcsManagementClientException {
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
    protected void bucketWipeCompleted(BucketWipeResult result, String id, String namespace) {
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

    @Override
    public Map<String, Object> changeNamespacePlan(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException {
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

    @Override
    public ServiceDefinitionProxy lookupServiceDefinition(String serviceDefinitionId) throws ServiceBrokerException {
        ServiceDefinitionProxy service = catalog.findServiceDefinition(serviceDefinitionId);
        if (service == null) {
            throw new ServiceInstanceDoesNotExistException(serviceDefinitionId);
        }
        return service;
    }

    @Override
    public String addExportToBucket(String bucket, String namespace, String relativeExportPath) throws EcsManagementClientException {
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

    public void grantUserLifecycleManagementPolicy(String prefixedBucket, String namespace, String username) {
        logger.info("Granting lifecycle management bucket policy on bucket '{}' to user '{}'", prefixedBucket, prefix(username));

        BucketPolicy policy = new BucketPolicy(BUCKET_POLICY_VERSION, "LifecycleManagementBucketPolicy", new ArrayList<>());
        if (BucketPolicyAction.exists(connection, prefixedBucket, namespace)) {
            policy = BucketPolicyAction.get(connection, prefixedBucket, namespace);
        }

        BucketPolicyStatement statement = new BucketPolicyStatement(
                getPolicyStatementId(username),
                new BucketPolicyEffect("Allow"),
                new BucketPolicyPrincipal(prefix(username)),
                new BucketPolicyActions(Arrays.asList(
                        S3_ACTION_PUT_LC_CONFIG,
                        S3_ACTION_GET_LC_CONFIG,
                        S3_ACTION_GET_BUCKET_POLICY
                )),
                new BucketPolicyResource(Collections.singletonList(prefixedBucket))
        );

        if (addPolicyStatement(policy, statement)) {
            logger.info("Updating bucket policy on bucket '{}' for user '{}'", prefixedBucket, prefix(username));
            BucketPolicyAction.update(connection, prefixedBucket, policy, namespace);
        } else {
            logger.info("Bucket policy '{}' already contains a statement for user '{}'", prefixedBucket, prefix(username));
        }
    }

    /**
     * Merge request bucket tags with plan and service provided tags
     * <p>
     * Request bucket tags are overwritten with plan and service ones,
     * while bucket tags provided in plan description are overwritten by service tags
     * since service settings are forced by administrator through the catalog
     */
    static List<Map<String, String>> mergeBucketTags(ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters) {
        List<Map<String, String>> serviceTags = (List<Map<String, String>>) service.getServiceSettings().get(TAGS);
        List<Map<String, String>> planTags = (List<Map<String, String>>) plan.getServiceSettings().get(TAGS);
        List<Map<String, String>> requestedTags = (List<Map<String, String>>) requestParameters.get(TAGS);
        List<Map<String, String>> unmatchedTags;

        if (planTags != null && serviceTags != null) {
            unmatchedTags = new ArrayList<>(planTags);

            for (Map<String, String> planTag : planTags) {
                for (Map<String, String> serviceTag : serviceTags) {
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

            for (Map<String, String> requestedTag : requestedTags) {
                for (Map<String, String> serviceTag : serviceTags) {
                    String r = requestedTag.get(KEY);
                    String s = serviceTag.get(KEY);
                    if (r != null && r.equals(s)) {
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
        List<Map<String, String>> requestedMetadata = (List<Map<String, String>>) requestParameters.get(SEARCH_METADATA);

        if (serviceMetadata == null) {
            return requestedMetadata;
        } else if (requestedMetadata == null) {
            return serviceMetadata;
        } else {
            List<Map<String, String>> unmatchedMetadata = new ArrayList<>(requestedMetadata);
            for (Map<String, String> requestedMetadatum : requestedMetadata) {
                for (Map<String, String> serviceMetadatum : serviceMetadata) {
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
    public static Map<String, Object> mergeParameters(BrokerConfig brokerConfig, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters) {
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

    @Override
    public Map<String, Object> mergeParameters(ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters) {
        return mergeParameters(broker, service, plan, requestParameters);
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
            logger.debug("Checking lifecycle management bucket policy on '{}'({}) with user '{}'", prefix(bucketName), namespace, prefix(user));
            BucketPolicy policy = BucketPolicyAction.get(connection, prefix(bucketName), namespace);
            BucketPolicyStatement bucketPolicyStatement = getPolicyStatementByPrincipal(policy, getPolicyStatementId(user));
            if (bucketPolicyStatement != null) actions = bucketPolicyStatement.getBucketPolicyAction();
        } catch (RuntimeException e) {
            logger.debug(
                    "Object user '{}' does not have lifecycle management bucket policy permissions for bucket '{}' in namespace '{}'",
                    prefix(user), prefix(bucketName), namespace
            );
        }

        // if user doesn't have full permissions, and also doesn't have R/W LC permissions, then grant them
        if (!actions.contains(S3_ACTION_ALL) && (!actions.contains(S3_ACTION_GET_LC_CONFIG) || !actions.contains(S3_ACTION_PUT_LC_CONFIG))) {
            grantUserLifecycleManagementPolicy(prefix(bucketName), namespace, user);
        }
    }

    void changeBucketExpiration(String bucketName, String namespace, int days) throws URISyntaxException {
        provideUserWithLifecycleManagementPolicy(bucketName, namespace, broker.getRepositoryUser());
        LifecycleConfiguration configuration = BucketExpirationAction.get(broker, namespace, prefix(bucketName));

        if (configuration == null || configuration.getRules() == null) {
            logger.info("Applying bucket expiration on '{}': {} days", bucketName, days);
            BucketExpirationAction.update(broker, namespace, prefix(bucketName), days, null);
        } else {
            List<LifecycleRule> rules = new ArrayList<>(configuration.getRules());
            for (LifecycleRule rule : rules) {
                if (rule.getStatus() == LifecycleRule.Status.Enabled && rule.getId().startsWith(BucketExpirationAction.RULE_PREFIX)) {
                    if (rule.getExpirationDays() != days) {
                        logger.info("Changing bucket expiration on '{}': {} days instead of {} days", bucketName, days, rule.getExpirationDays());
                        rules.remove(rule);
                        BucketExpirationAction.update(broker, namespace, prefix(bucketName), days, rules);
                    }
                    return;
                }
            }
            logger.info("Applying bucket expiration on '{}': {} days", bucketName, days);
            BucketExpirationAction.update(broker, namespace, prefix(bucketName), days, rules);
        }
    }

    void deleteCurrentExpirationRule(String bucketName, String namespace) throws URISyntaxException {
        provideUserWithLifecycleManagementPolicy(bucketName, namespace, broker.getRepositoryUser());

        LifecycleConfiguration configuration = BucketExpirationAction.get(broker, namespace, prefix(bucketName));

        if (configuration != null && configuration.getRules() != null) {
            List<LifecycleRule> rules = new ArrayList<>(configuration.getRules());
            for (LifecycleRule rule : rules) {
                if (rule.getStatus() == LifecycleRule.Status.Enabled && rule.getId().startsWith(BucketExpirationAction.RULE_PREFIX)) {
                    logger.info("Removing bucket expiration rule on bucket '{}' in '{}' ({} days)", prefix(bucketName), namespace, rule.getExpirationDays());
                    rules.remove(rule);
                    BucketExpirationAction.delete(broker, namespace, prefix(bucketName), rule.getId(), rules);
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
}
