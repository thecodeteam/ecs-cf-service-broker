package com.emc.ecs.servicebroker.service.s3;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.S3Exception;
import com.emc.object.s3.bean.LifecycleConfiguration;
import com.emc.object.s3.bean.LifecycleRule;
import com.emc.object.s3.jersey.S3JerseyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

public final class BucketExpirationAction {

    private static final Logger logger = LoggerFactory.getLogger(BucketExpirationAction.class);
    public static String RULE_PREFIX = "broker-lifecycle-rule-";

    public static void update(BrokerConfig brokerConfig, String namespace, String bucketName, int days, List<LifecycleRule> currentRules) throws URISyntaxException {
        if (days < 0) {
            throw new IllegalArgumentException("Invalid expiration days set on '" + bucketName + "' bucket: Expiration days could not be less then 0");
        }

        LifecycleRule newRule = new LifecycleRule(
                RULE_PREFIX + UUID.randomUUID().toString(), '0' + bucketName, LifecycleRule.Status.Enabled
        ).withExpirationDays(days);

        S3Client s3Client = createS3Client(brokerConfig, namespace);

        if (currentRules == null) {
            logger.debug("Adding bucket lifecycle rule {}: expiration days = {}", newRule.getPrefix(), newRule.getExpirationDays());
            s3Client.setBucketLifecycle(bucketName, new LifecycleConfiguration().withRules(newRule));
        } else {
            newRule.setPrefix(currentRules.size() + bucketName);
            currentRules.add(newRule);
            logger.debug("Adding bucket lifecycle rule {}: expiration days = {}", newRule.getPrefix(), newRule.getExpirationDays());
            s3Client.setBucketLifecycle(bucketName, new LifecycleConfiguration().withRules(currentRules));
        }
    }

    public static LifecycleConfiguration get(BrokerConfig brokerConfig, String namespace, String bucketName) throws URISyntaxException {
        S3Client s3Client = createS3Client(brokerConfig, namespace);

        LifecycleConfiguration lifecycle;
        try {
            lifecycle = s3Client.getBucketLifecycle(bucketName);
        } catch (S3Exception exception) {
            logger.warn("Object user '{}' does not have Lifecycle Management policy on bucket '{}'", brokerConfig.getPrefixedUserName(), bucketName);
            return null;
        }

        return lifecycle;
    }

    public static void delete(BrokerConfig brokerConfig, String namespace, String bucketName, String ruleId, List<LifecycleRule> rules) throws URISyntaxException {
        S3Client s3Client = createS3Client(brokerConfig, namespace);

        for (LifecycleRule rule : rules) {
            if (rule.getId().equals(ruleId)) {
                rules.remove(rule);
                break;
            }
        }

        if (rules.isEmpty()) {
            s3Client.deleteBucketLifecycle(bucketName);
        } else {
            s3Client.setBucketLifecycle(bucketName, new LifecycleConfiguration().withRules(rules));
        }
    }

    private static S3Client createS3Client(BrokerConfig brokerConfig, String namespace) throws URISyntaxException {
        S3Config s3Config = S3ConfigUtils.s3Config(brokerConfig);

        if (namespace != null) {
            s3Config.setNamespace(namespace);
        }

        return new S3JerseyClient(s3Config);
    }
}
