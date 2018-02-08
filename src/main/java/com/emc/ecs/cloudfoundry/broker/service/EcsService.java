package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.BrokerConfig;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.management.sdk.*;
import com.emc.ecs.management.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
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

    @Autowired
    private Connection connection;

    @Autowired
    private BrokerConfig broker;

    @Autowired
    private CatalogConfig catalog;

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
        try {
            lookupObjectEndpoints();
            lookupReplicationGroupID();
            prepareRepository();
        } catch (EcsManagementClientException | EcsManagementResourceNotFoundException e) {
            throw new ServiceBrokerException(e);
        }
    }

    void deleteBucket(String id) {
        try {
            BucketAction.delete(connection, prefix(id), broker.getNamespace());
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
            parameters.putAll(plan.getServiceSettings());
            parameters.putAll(service.getServiceSettings());

            BucketAction.create(connection, new ObjectBucketCreate(prefix(id),
                    broker.getNamespace(), replicationGroupID, parameters));

            if (parameters.containsKey(QUOTA)) {
                logger.info("Applying quota");
                @SuppressWarnings(UNCHECKED)
                Map<String, Integer> quota = (Map<String, Integer>) parameters
                        .get(QUOTA);
                BucketQuotaAction.create(connection, prefix(id),
                        broker.getNamespace(), quota.get(LIMIT), quota.get(WARN));
            }

            if (parameters.containsKey(DEFAULT_RETENTION)) {
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
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());
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

    UserSecretKey createUser(String id) {
        try {
            ObjectUserAction.create(connection, prefix(id), broker.getNamespace());
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
        ObjectUserAction.delete(connection, prefix(id));
    }

    void addUserToBucket(String id, String username) {
        try {
            addUserToBucket(id, username, Collections.singletonList("full_control"));
        } catch (Exception e) {
            throw new ServiceBrokerException(e);
        }
    }

    void addUserToBucket(String id, String username,
                         List<String> permissions) throws EcsManagementClientException {
        BucketAcl acl = BucketAclAction.get(connection, prefix(id),
                broker.getNamespace());
        List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
        userAcl.add(new BucketUserAcl(prefix(username), permissions));
        acl.getAcl().setUserAccessList(userAcl);
        BucketAclAction.update(connection, prefix(id), acl);
    }

    void removeUserFromBucket(String id, String username)
            throws EcsManagementClientException {
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

    private void lookupObjectEndpoints() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
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

    String getNamespaceURL(String namespace, ServiceDefinitionProxy service,
                           PlanProxy plan, Map<String, Object> parameters) {
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());

        try {
            return getNamespaceURL(namespace, parameters);
        } catch (EcsManagementClientException e) {
            throw new ServiceBrokerException(e);
        }
    }

    private String getNamespaceURL(String namespace,
                                   Map<String, Object> parameters)
            throws EcsManagementClientException {
        String baseUrl = (String) parameters.getOrDefault("base-url",
                broker.getBaseUrl());
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

    private void prepareRepository() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        String bucketName = broker.getRepositoryBucket();
        String userName = broker.getRepositoryUser();
        if (!bucketExists(bucketName)) {
            ServiceDefinitionProxy service = lookupServiceDefinition(
                    broker.getRepositoryServiceId());
            Map<String, Object> parameters = new HashMap<>();
            createBucket(bucketName, service,
                    service.findPlan(broker.getRepositoryPlanId()), parameters);
        }

        if (!userExists(userName)) {
            UserSecretKey secretKey = createUser(userName);
            addUserToBucket(bucketName, userName);
            broker.setRepositorySecret(secretKey.getSecretKey());
        } else {
            broker.setRepositorySecret(getUserSecret(userName));
        }
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

    private Boolean namespaceExists(String id)
            throws EcsManagementClientException {
        return NamespaceAction.exists(connection, prefix(id));
    }

    Map<String, Object> createNamespace(String id, ServiceDefinitionProxy service,
                                        PlanProxy plan, Map<String, Object> parameters)
            throws EcsManagementClientException {
        if (namespaceExists(id))
            throw new ServiceInstanceExistsException(id, service.getId());
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());
        NamespaceAction.create(connection, new NamespaceCreate(prefix(id),
                replicationGroupID, parameters));

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
        NamespaceAction.delete(connection, prefix(id));
    }

    Map<String, Object> changeNamespacePlan(String id, ServiceDefinitionProxy service,
                                            PlanProxy plan, Map<String, Object> parameters)
            throws EcsManagementClientException {
        parameters.putAll(plan.getServiceSettings());
        parameters.putAll(service.getServiceSettings());
        NamespaceAction.update(connection, prefix(id),
                new NamespaceUpdate(parameters));

        if (parameters.containsKey(RETENTION)) {
            @SuppressWarnings(UNCHECKED)
            Map<String, Integer> retention = (Map<String, Integer>) parameters
                    .get(RETENTION);
            for (Map.Entry<String, Integer> entry : retention.entrySet()) {
                if (NamespaceRetentionAction.exists(connection, id,
                        entry.getKey())) {
                    if (-1 == entry.getValue()) {
                        NamespaceRetentionAction.delete(connection, prefix(id),
                                entry.getKey());
                        parameters.remove(RETENTION);
                    } else {
                        NamespaceRetentionAction.update(connection, prefix(id),
                                entry.getKey(),
                                new RetentionClassUpdate(entry.getValue()));
                    }
                } else {
                    NamespaceRetentionAction.create(connection, prefix(id),
                            new RetentionClassCreate(entry.getKey(),
                                    entry.getValue()));
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
        String namespace = broker.getNamespace();
        String absoluteExportPath = "/" + namespace + "/" + prefix(instanceId) + "/" + relativeExportPath;
        List<NFSExport> exports = NFSExportAction.list(connection, absoluteExportPath);
        if (exports == null) {
            NFSExportAction.create(connection, absoluteExportPath);
        }
        return absoluteExportPath;
    }
}