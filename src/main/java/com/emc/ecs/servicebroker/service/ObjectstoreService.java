package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.ObjectscaleGatewayConnection;
import com.emc.ecs.management.sdk.actions.iam.IAMAccessKeyAction;
import com.emc.ecs.management.sdk.actions.iam.IAMPolicyAction;
import com.emc.ecs.management.sdk.actions.iam.IAMUserAction;
import com.emc.ecs.management.sdk.actions.iam.IAMUserPolicyAction;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.management.sdk.model.iam.policy.IamPolicy;
import com.emc.ecs.management.sdk.model.iam.policy.document.IAMPolicyDocument;
import com.emc.ecs.management.sdk.model.iam.policy.document.IAMPolicyStatement;
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
import java.util.*;

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

        /*
        // TODO remove this after dev
        long t = System.currentTimeMillis();
        for (int i = 1; i <= 20; i++) {
            String userName = "test-user-limit-1639473560712-" + i;
//            String userName = "test-user-limit-" + t + "-" + i;
            //createUser(userName, account);
            deleteUser(userName, account);
//            addUserToBucket(bucketName, account, userName);
        }
         */
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
        String objectscaleId = "oscib74ceaf797714e7e";
        String objectstoreId = "osti8fd659aa22ea84d6";

        // 1. Create policy
        String bucketARN = "arn:aws:s3:" + objectscaleId + ":" + objectstoreId + ":" + bucketId;
        String objectsARN = bucketARN + "/*";

        /*
        IAMPolicyDocument policy = new IAMPolicyDocument(
                "2021-10-17", null,
                Arrays.asList(
                        new IAMPolicyStatement()
                                .resource(bucketARN)
                                .effect("Allow")
                                .action("s3:ListBucket"),
                        new IAMPolicyStatement()
                                .resource(objectsARN)
                                .effect("Allow")
                                .action(Arrays.asList(
                                        "s3:PutObject",
                                        "s3:PutObjectAcl",
                                        "s3:GetObject",
                                        "s3:GetObjectAcl",
                                        "s3:DeleteObject"
                                ))
                )
        );

        String policyDocument = policy.toString(); // TODO implement convert to json
*/
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

        String policyName = policyName(bucketId, FULL_CONTROL);

        String policyArn = "urn:osc:iam::" + accountId + ":policy/ecs-cf-broker-1-policy";

        IamPolicy iamPolicy = IAMPolicyAction.get(objectscaleGateway, policyArn, accountId);
        if (iamPolicy == null) {
            iamPolicy = IAMPolicyAction.create(objectscaleGateway, policyName, policyDocument, accountId);
        }

        // 2. add policy to user
        IAMUserPolicyAction.attach(objectscaleGateway, username, iamPolicy.getArn(), accountId);
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
    public void removeUserFromBucket(String bucketId, String accountId, String username) throws EcsManagementClientException {
        String policyName = policyName(bucketId, FULL_CONTROL);     // TODO restore policy name or ARN from binding instance - what happens when we have custom permissions?
        String policyARN = "urn:osc:iam::" + accountId + ":policy/" + policyName;
        IAMUserPolicyAction.detach(objectscaleGateway, username, policyARN, accountId);
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
