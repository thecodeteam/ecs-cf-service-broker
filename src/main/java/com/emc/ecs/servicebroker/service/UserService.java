package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.actions.*;
import com.emc.ecs.management.sdk.actions.iam.IAMAccessKeyAction;
import com.emc.ecs.management.sdk.actions.iam.IAMPolicyAction;
import com.emc.ecs.management.sdk.actions.iam.IAMUserAction;
import com.emc.ecs.management.sdk.actions.iam.IAMUserPolicyAction;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.management.sdk.model.iam.policy.IamPolicy;
import com.emc.ecs.management.sdk.model.iam.user.IamAccessKey;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECTSCALE;
import static com.emc.ecs.servicebroker.model.Constants.FULL_CONTROL;
import static org.apache.commons.collections.ListUtils.isEqualList;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public static final String FULL_CONTROL_PERMISSIONS_LIST = String.join(",",
            "s3:PutObject",
            "s3:PutObjectAcl",
            "s3:GetObject",
            "s3:GetObjectAcl",
            "s3:DeleteObject"
    );

    @Autowired
    private BrokerConfig broker;

    public UserSecretKey createUser(ManagementAPIConnection connection, String userId, String namespace) {
        if (isIamManager()) {
            return createIamUser(connection, userId, namespace);
        }

        return createObjectUser(connection, userId, namespace);
    }

    public void deleteUser(ManagementAPIConnection connection, String userId, String namespace) {
        if (isIamManager()) {
            deleteIamUser(connection, userId, namespace);
        } else {
            deleteObjectUser(connection, userId, namespace);
        }
    }

    public boolean userExists(ManagementAPIConnection connection, String userId, String namespace) {
        if (isIamManager()) {
            return iamUserExists(connection, userId, namespace);
        }

        return objectUserExists(connection, userId, namespace);
    }

    public void addUserToBucket(ManagementAPIConnection connection, String bucketId, String namespace, String username, List<String> permissions) {
        if (isIamManager()) {
            if (OBJECTSCALE.equals(broker.getApiType())) {
                addObjectStoreIamUserToBucket(connection, bucketId, namespace, username);
            } else {
                addEcsIamUserToBucket(connection, bucketId, namespace, username, permissions);
            }
        } else {
            addObjectUserToBucket(connection, bucketId, namespace, username, permissions);
        }
    }

    public void removeUserFromBucket(ManagementAPIConnection connection, String bucketId, String namespace, String username) {
        if (isIamManager()) {
            if (OBJECTSCALE.equals(broker.getApiType())) {
                removeObjectStoreIamUserFromBucket(connection, bucketId, namespace, username);
            } else {
                removeEcsIamUserFromBucket(connection, bucketId, namespace, username);
            }
        } else {
            removeObjectUserFromBucket(connection, bucketId, namespace, username);
        }
    }

    boolean isIamManager() {
        return OBJECTSCALE.equals(broker.getApiType()) || broker.isIamManager();
    }


    private UserSecretKey createObjectUser(ManagementAPIConnection connection, String userId, String namespace) {

        logger.info("Creating object user '{}' in namespace '{}'", userId, namespace);
        ObjectUserAction.create(connection, userId, namespace);

        logger.info("Creating secret for user '{}'", userId);
        ObjectUserSecretAction.create(connection, userId);

        UserSecretKey userSecretKey = ObjectUserSecretAction.list(connection, userId).get(0);

        return userSecretKey;
    }

    private UserSecretKey createIamUser(ManagementAPIConnection connection, String userId, String namespace) {
        logger.info("Creating IAM user '{}' in account '{}'", userId, namespace);
        IAMUserAction.create(connection, userId, namespace);

        logger.info("Creating access key for IAM user '{}'", userId);
        IamAccessKey iamKey = IAMAccessKeyAction.create(connection, userId, namespace);

        logger.info("Access key received");
        UserSecretKey key = new UserSecretKey();
        key.setAccessKey(iamKey.getAccessKeyId());
        key.setSecretKey(iamKey.getSecretAccessKey());
        key.setKeyTimestamp(iamKey.getCreateDate());
        return key;
    }

    private void deleteObjectUser(ManagementAPIConnection connection, String userId, String namespace) {
        if (objectUserExists(connection, userId, namespace)) {
            logger.info("Deleting user '{}' in namespace '{}'", userId, namespace);
            ObjectUserAction.delete(connection, userId);
        } else {
            logger.info("User {} no longer exists, assume already deleted", userId);
        }
    }


    private void deleteIamUser(ManagementAPIConnection connection, String userId, String namespace) {
        if (iamUserExists(connection, userId, namespace)) {
            logger.info("Deleting access keys of IAM user '{}' in account '{}'", userId, namespace);
            List<IamAccessKey> accessKeys = IAMAccessKeyAction.list(connection, userId, namespace);
            for (IamAccessKey key : accessKeys) {
                IAMAccessKeyAction.delete(connection, key.getAccessKeyId(), userId, namespace);
            }

            logger.info("Deleting IAM user '{}' in account '{}'", userId, namespace);
            IAMUserAction.delete(connection, userId, namespace);
        } else {
            logger.info("User {} no longer exists, assume already deleted", userId);
        }
    }

    private void addObjectUserToBucket(ManagementAPIConnection connection, String bucketId, String namespace, String username, List<String> permissions) {
        logger.info("Adding user '{}' to bucket '{}' in '{}' with {} access", username, bucketId, namespace, permissions);

        BucketAcl acl = BucketAclAction.get(connection, bucketId, namespace);

        List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
        userAcl.add(new BucketUserAcl(username, permissions));
        acl.getAcl().setUserAccessList(userAcl);

        BucketAclAction.update(connection, bucketId, acl);

        if (!BucketAction.get(connection, bucketId, namespace).getFsAccessEnabled()) {
            BucketPolicy bucketPolicy = new BucketPolicy(
                    "2012-10-17",
                    "DefaultPCFBucketPolicy",
                    new BucketPolicyStatement("DefaultAllowTotalAccess",
                            new BucketPolicyEffect("Allow"),
                            new BucketPolicyPrincipal(username),
                            new BucketPolicyActions(Collections.singletonList("s3:*")),
                            new BucketPolicyResource(Collections.singletonList(bucketId))
                    )
            );
            BucketPolicyAction.update(connection, bucketId, bucketPolicy, namespace);
        }
    }

    private void addObjectStoreIamUserToBucket(ManagementAPIConnection connection, String bucketId, String namespace, String username) {
        logger.info("Adding user '{}' default access to bucket '{}' in '{}'", username, bucketId, namespace);

        // TODO get them from broker config?

        String objectscaleId = broker.getObjectscaleId();
        String objectstoreId = broker.getObjectstoreId();

        // 1. Create policy
        String bucketARN = "arn:aws:s3:" + objectscaleId + ":" + objectstoreId + ":" + namespace;
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
                "         \"Action\":[" + FULL_CONTROL_PERMISSIONS_LIST + "],\n" +
                "         \"Resource\":\"" + objectsARN + "\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";

        String policyName = policyName(bucketId, FULL_CONTROL);

        IamPolicy iamPolicy = IAMPolicyAction.get(connection, policyName, namespace);
        if (iamPolicy == null) {
            iamPolicy = IAMPolicyAction.create(connection, policyName, policyDocument, namespace);
        }

        // 2. add policy to user
        IAMUserPolicyAction.attach(connection, username, iamPolicy.getArn(), namespace);
    }


    private void addEcsIamUserToBucket(ManagementAPIConnection connection, String bucketId, String namespace, String username, List<String> permissions) {
        logger.info("Adding Iam user '{}' to bucket '{}' in '{}' with {} access", username, bucketId, namespace, permissions);

        // 1. Create policy
        String permissionsList = buildPermissionsList(permissions);

        String policyDocument = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Action\": [" + permissionsList + " ],\n" +
                "      \"Resource\": \"*\",\n" +
                "      \"Effect\": \"Allow\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String policyName = policyName(bucketId, permissions);
        String arn = "urn:ecs:iam::" + namespace + ":policy/" + policyName;

        IamPolicy iamPolicy = IAMPolicyAction.get(connection, arn, namespace);
        if (iamPolicy == null) {
            iamPolicy = IAMPolicyAction.create(connection, policyName, policyDocument, namespace);
        }

        // 2. add policy to user
        IAMUserPolicyAction.attach(connection, username, iamPolicy.getArn(), namespace);
    }

    private void removeObjectUserFromBucket(ManagementAPIConnection connection, String bucket, String namespace, String username) {
        if (!aclExists(connection, bucket, namespace)) {
            logger.info("ACL {} no longer exists when removing user {}", bucket, username);
            return;
        }

        BucketAcl acl = BucketAclAction.get(connection, bucket, namespace);

        List<BucketUserAcl> newUserAcl = acl.getAcl().getUserAccessList()
                .stream().filter(a -> !a.getUser().equals(username))
                .collect(Collectors.toList());
        acl.getAcl().setUserAccessList(newUserAcl);

        BucketAclAction.update(connection, bucket, acl);
    }

    private void removeObjectStoreIamUserFromBucket(ManagementAPIConnection connection, String bucketId, String accountId, String userId) {
        String policyName = policyName(bucketId, FULL_CONTROL);
        IamPolicy iamPolicy = IAMPolicyAction.get(connection, policyName, accountId);
        if (iamPolicy != null) {
            IAMUserPolicyAction.detach(connection, userId, iamPolicy.getArn(), accountId);
        } else {
            logger.warn("Cannot find iam policy to remove from user: " + policyName);
        }
    }

    private void removeEcsIamUserFromBucket(ManagementAPIConnection connection, String bucket, String namespace, String username) {
        String policyName = policyName(bucket, FULL_CONTROL);
        String arn = "urn:ecs:iam::" + namespace + ":policy/" + policyName;
        IamPolicy iamPolicy = IAMPolicyAction.get(connection, arn, namespace);
        if (iamPolicy != null) {
            IAMUserPolicyAction.detach(connection, username, iamPolicy.getArn(), namespace);
        } else {
            logger.warn("Cannot find iam policy to remove from user: " + policyName);
        }
    }

    private boolean iamUserExists(ManagementAPIConnection connection, String userId, String namespace) {
        return IAMUserAction.exists(connection, userId, namespace);
    }

    private boolean objectUserExists(ManagementAPIConnection connection, String userId, String namespace) {
        return ObjectUserAction.exists(connection, userId, namespace);
    }

    private String policyName(String bucketId, List<String> permissions) {
        if (permissions == null || isEqualList(FULL_CONTROL, permissions)) {
            return bucketId + "-policy";
        } else {
            List<String> copy = new ArrayList<>(permissions);
            Collections.sort(copy);
            return bucketId + "-policy-" + String.join("-", copy);
        }
    }

    private boolean aclExists(ManagementAPIConnection connection, String bucketName, String namespace) throws EcsManagementClientException {
        return BucketAclAction.exists(connection, bucketName, namespace);
    }

    public static String buildPermissionsList(List<String> permissions) {
        if (permissions == null || permissions.size() == 0 || isEqualList(FULL_CONTROL, permissions)) {
            return FULL_CONTROL_PERMISSIONS_LIST;
        }

        StringBuilder sb = new StringBuilder();
        for (String permission : permissions) {
            sb.append('"');
            if (!permission.startsWith("s3:")) {
                sb.append("s3:");
            }
            sb.append(permission);
            sb.append('"');
            sb.append(',');
        }

        if (permissions.size() > 1) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}
