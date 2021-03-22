package com.emc.ecs.servicebroker.service.s3;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.LifecycleConfiguration;
import com.emc.object.s3.bean.LifecycleRule;
import com.emc.object.s3.jersey.S3JerseyClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

public final class BucketExpirationAction {

    public static void setBucketExpiration(BrokerConfig broker, String bucketName, int days) throws URISyntaxException {
        if (days < 0) {
            throw new IllegalArgumentException("Invalid expiration days set on '" + bucketName + "' bucket: Expiration days could not be less then 0");
        }
        S3Config s3Config = new S3Config(new URI(broker.getRepositoryEndpoint()));
        s3Config.withIdentity(broker.getPrefixedUserName()).withSecretKey(broker.getRepositorySecret());
        S3Client s3Client = new S3JerseyClient(s3Config);

        s3Client.setBucketLifecycle(bucketName, new LifecycleConfiguration().withRules(
                new LifecycleRule(UUID.randomUUID().toString(), broker.getPrefix(), LifecycleRule.Status.Enabled).withExpirationDays(days)));
    }

    public static int getBucketExpiration(BrokerConfig broker, String bucketName) throws URISyntaxException {
        S3Config s3Config = new S3Config(new URI(broker.getRepositoryEndpoint()));
        s3Config.withIdentity(broker.getPrefixedUserName()).withSecretKey(broker.getRepositorySecret());
        S3Client s3Client = new S3JerseyClient(s3Config);

        LifecycleConfiguration lifecycle = s3Client.getBucketLifecycle(bucketName);
        List<LifecycleRule> rules = lifecycle.getRules();

        for (LifecycleRule rule: rules) {
            if (rule.getStatus() == LifecycleRule.Status.Enabled) {
                return rule.getExpirationDays();
            }
        }
        return 0;
    }
}
