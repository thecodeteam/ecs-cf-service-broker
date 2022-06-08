package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.ObjectscaleGatewayConnection;
import com.emc.ecs.management.sdk.actions.iam.IAMAccessKeyAction;
import com.emc.ecs.management.sdk.actions.iam.IAMPolicyAction;
import com.emc.ecs.management.sdk.actions.iam.IAMUserAction;
import com.emc.ecs.management.sdk.actions.iam.IAMUserPolicyAction;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.management.sdk.model.iam.policy.IamPolicy;
import com.emc.ecs.management.sdk.model.iam.user.IamAccessKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.object.s3.S3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECTSCALE;
import static com.emc.ecs.servicebroker.model.Constants.FULL_CONTROL;
import static org.apache.commons.collections.ListUtils.isEqualList;

@Service
public class ObjectstoreService extends EcsService {
    private static final Logger logger = LoggerFactory.getLogger(ObjectstoreService.class);

    @Autowired
    private ObjectscaleGatewayConnection objectscaleGateway;

    @Autowired
    private S3Client s3Client;

    @PostConstruct
    void initialize() {
        if (broker.isConfigValidationMode()) {
            logger.info("Skipping ObjectStore service initialization - working in validation mode");
            return;
        }

        if (OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            logger.info("Initializing Objectstore service with management endpoint {}", broker.getObjectstoreManagementEndpoint());

            try {
                prepareRepository();
                prepareBucketWipe();
            } catch (EcsManagementClientException | URISyntaxException e) {
                logger.error("Failed to initialize Objectscale service: {}", e.getMessage());
                throw new ServiceBrokerException(e.getMessage(), e);
            }
        }
    }

    private void prepareRepository() throws EcsManagementClientException {
        String bucketName = broker.getRepositoryBucket();
        String account = broker.getAccountId();

        prepareRepositoryBucket(bucketName, account);
    }

    protected void prepareBucketWipe() throws URISyntaxException {
        bucketWipe = bucketWipeFactory.getBucketWipe(s3Client);
    }

    @Override
    public String getDefaultNamespace() {
        return broker.getAccountId();
    }

    @Override
    public String getObjectEndpoint() {
        return broker.getObjectstoreS3Endpoint();
    }

    @Override
    public UserSecretKey createUser(String id, String accountId) {
        try {
            String userId = prefix(id);

            logger.info("Creating user '{}' in account '{}'", userId, accountId);
            IAMUserAction.create(objectscaleGateway, userId, accountId);

            logger.info("Creating secret for user '{}'", userId);
            IamAccessKey iamKey = IAMAccessKeyAction.create(objectscaleGateway, userId, accountId);

            UserSecretKey key = new UserSecretKey();
            key.setAccessKey(iamKey.getAccessKeyId());
            key.setSecretKey(iamKey.getSecretAccessKey());
            key.setKeyTimestamp(iamKey.getCreateDate());
            return key;
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String userId, String accountId) throws EcsManagementClientException {
        try {
            if (userExists(userId, accountId)) {
                logger.info("Deleting access keys of user '{}' in account '{}'", userId, accountId);
                List<IamAccessKey> accessKeys = IAMAccessKeyAction.list(objectscaleGateway, prefix(userId), accountId);
                for (IamAccessKey key : accessKeys) {
                    IAMAccessKeyAction.delete(objectscaleGateway, key.getAccessKeyId(), prefix(userId), accountId);
                }

                logger.info("Deleting user '{}' in account '{}'", userId, accountId);
                IAMUserAction.delete(objectscaleGateway, prefix(userId), accountId);
            } else {
                logger.info("User {} no longer exists, assume already deleted", prefix(userId));
            }
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean userExists(String userId, String accountId) throws ServiceBrokerException {
        try {
            return IAMUserAction.exists(objectscaleGateway, prefix(userId), accountId);
        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void addUserToBucket(String bucketId, String accountId, String username) {
        logger.info("Adding user '{}' default access to bucket '{}' in '{}'", prefix(username), prefix(bucketId), accountId);

        // TODO get them from broker config?

        String objectscaleId = broker.getObjectscaleId();
        String objectstoreId = broker.getObjectstoreId();

        // 1. Create policy
        String bucketARN = "arn:aws:s3:" + objectscaleId + ":" + objectstoreId + ":" + prefix(bucketId);
        String objectsARN = bucketARN + "/*";

        String policyDocument = "{\n" +
                "   \"Version\":\"2012-10-17\",\n" +
                "   \"Statement\":[\n" +
                "      {\n" +
                "         \"Effect\":\"Allow\",\n" +
                "         \"Action\":[\"s3:ListBucket\"],\n" +
                "         \"Resource\":\"" + bucketARN + "\"\n" +
                "      },\n" +
                "      {\n" +
                "         \"Effect\":\"Allow\",\n" +
                "         \"Action\":[\n" +
                "            \"s3:PutObject\",\n" +
                "            \"s3:PutObjectAcl\",\n" +
                "            \"s3:GetObject\",\n" +
                "            \"s3:GetObjectAcl\",\n" +
                "            \"s3:DeleteObject\"\n" +
                "         ],\n" +
                "         \"Resource\":\"" + objectsARN + "\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";

        String policyName = policyName(prefix(bucketId), FULL_CONTROL);

        IamPolicy iamPolicy = IAMPolicyAction.get(objectscaleGateway, policyName, accountId);
        if (iamPolicy == null) {
            iamPolicy = IAMPolicyAction.create(objectscaleGateway, policyName, policyDocument, accountId);
        }

        // 2. add policy to user
        IAMUserPolicyAction.attach(objectscaleGateway, prefix(username), iamPolicy.getArn(), accountId);
    }

    private String policyName(String bucketId, List<String> permissions) {
        if (permissions == null || isEqualList(FULL_CONTROL, permissions)) {
            return prefix(bucketId) + "-policy";
        } else {
            List<String> copy = new ArrayList<>(permissions);
            Collections.sort(copy);
            String join = String.join("-", copy);
            return prefix(bucketId) + "-policy-" + join;
        }
    }

    @Override
    public void removeUserFromBucket(String bucketId, String accountId, String userId) throws EcsManagementClientException {
        String policyName = policyName(prefix(bucketId), FULL_CONTROL);
        IamPolicy iamPolicy = IAMPolicyAction.get(objectscaleGateway, policyName, accountId);
        if (iamPolicy != null) {
            IAMUserPolicyAction.detach(objectscaleGateway, prefix(userId), iamPolicy.getArn(), accountId);
        } else {
            logger.warn("Cannot find iamPolicy to remove from user: " + policyName);
        }

    }

    @Override
    public void addUserToBucket(String bucketId, String namespace, String username, List<String> permissions) throws EcsManagementClientException {
        if (permissions == null || isEqualList(FULL_CONTROL, permissions)) {
            addUserToBucket(bucketId, namespace, username);
        } else {
            throw new UnsupportedOperationException("Not supported for Objectscale - user permissions");
        }
    }

    @Override
    public boolean namespaceExists(String namespace) throws EcsManagementClientException {
        // namespace operations are not supported for Objectscale
        // TODO check account existence
        return true;
    }

    @Override
    public String getNamespaceURL(String namespace, Map<String, Object> requestParameters, Map<String, Object> serviceSettings) {
        throw new UnsupportedOperationException("Not supported for Objectscale");
    }

    @Override
    public String getNamespaceURL(String namespace, Boolean useSSL, String baseUrl) throws EcsManagementClientException {
        throw new UnsupportedOperationException("Not supported for Objectscale");
    }

    @Override
    public Map<String, Object> createNamespace(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException {
        throw new UnsupportedOperationException("Not supported for Objectscale");
    }

    @Override
    public void deleteNamespace(String namespace) throws EcsManagementClientException {
        throw new UnsupportedOperationException("Not supported for Objectscale");
    }

    @Override
    public Map<String, Object> changeNamespacePlan(String namespace, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters) throws EcsManagementClientException {
        throw new UnsupportedOperationException("Not supported for Objectscale");
    }
}
