package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface StorageService {
    String getObjectEndpoint(); // TODO refactor usages (or rename to s3 endpoint?)

    Map<String, Object> getBrokerConfig();

    String getNfsMountHost();

    String prefix(String string);

    String getDefaultNamespace();

    ServiceDefinitionProxy lookupServiceDefinition(String serviceDefinitionId) throws ServiceBrokerException;

    Boolean getBucketFileEnabled(String bucketName, String namespace) throws EcsManagementClientException;

    Map<String, Object> mergeParameters(ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> requestParameters); // TODO refactor usages or rename

    @SuppressWarnings("unchecked")
    Map<String, Object> createBucket(String serviceInstanceId, String bucketName, ServiceDefinitionProxy serviceDefinition, PlanProxy plan, Map<String, Object> parameters);

    Map<String, Object> changeBucketPlan(String bucketName, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters, Map<String, Object> instanceSettings);

    CompletableFuture deleteBucket(String bucketName, String namespace);

    CompletableFuture wipeAndDeleteBucket(String id, String namespace);

    UserSecretKey createUser(String id, String namespace);

    void deleteUser(String userId, String namespace) throws EcsManagementClientException;

    Boolean userExists(String userId, String namespace) throws ServiceBrokerException;

    void addUserToBucket(String bucketId, String namespace, String username);

    void addUserToBucket(String bucketId, String namespace, String username, List<String> permissions) throws EcsManagementClientException;

    void removeUserFromBucket(String bucket, String namespace, String username) throws EcsManagementClientException;

    void createUserMap(String username, String namespace, int uid) throws EcsManagementClientException;

    void deleteUserMap(String username, String namespace, String uid) throws EcsManagementClientException;

    String getNamespaceURL(String namespace, Map<String, Object> requestParameters, Map<String, Object> serviceSettings);

    String getNamespaceURL(String namespace, Boolean useSSL, String baseUrl) throws EcsManagementClientException;

    Map<String, Object> createNamespace(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException;

    void deleteNamespace(String namespace) throws EcsManagementClientException;

    Map<String, Object> changeNamespacePlan(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException;

    String addExportToBucket(String bucket, String namespace, String relativeExportPath) throws EcsManagementClientException;
}
