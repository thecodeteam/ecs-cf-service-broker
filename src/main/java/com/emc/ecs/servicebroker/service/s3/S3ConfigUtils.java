package com.emc.ecs.servicebroker.service.s3;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.object.s3.S3Config;

import java.net.URI;
import java.net.URISyntaxException;

public class S3ConfigUtils {
    public static S3Config s3Config(BrokerConfig config) throws URISyntaxException {
        switch (config.getApiType().toLowerCase()) {
            case "objectscale":
                return objectstoreConfig(config);
            case "ecs":
            default:
                return ecsConfig(config);
        }
    }

    public static S3Config objectstoreConfig(BrokerConfig config) throws URISyntaxException {
        String repositoryEndpoint = config.getObjectstoreS3Endpoint();
        String userName = config.getAccessKey();
        String repositorySecret = config.getSecretKey();
        String accountId = config.getAccountId();
        boolean useV2Signer = !config.isAwsSignatureV4();

        return new S3Config(new URI(repositoryEndpoint))
                .withUseV2Signer(useV2Signer)
                .withNamespace(accountId)
                .withIdentity(userName)
                .withSecretKey(repositorySecret);
    }

    public static S3Config ecsConfig(BrokerConfig config) throws URISyntaxException {
        String repositoryEndpoint = config.getRepositoryEndpoint();
        String userName = config.getPrefixedUserName();
        String repositorySecret = config.getRepositorySecret();
        boolean useV2Signer = !config.isAwsSignatureV4();

        return new S3Config(new URI(repositoryEndpoint))
                .withUseV2Signer(useV2Signer)
                .withIdentity(userName)
                .withSecretKey(repositorySecret);
    }
}
