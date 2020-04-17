package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.tool.BucketWipeOperations;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.jersey.S3JerseyClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Used to create instances of the BucketWipeOperations based on a given broker config
 */
public class BucketWipeFactory {

    public BucketWipeOperations getBucketWipe(BrokerConfig broker) throws URISyntaxException {
        S3Config s3Config = new S3Config(new URI(broker.getRepositoryEndpoint()));
        s3Config.withIdentity(broker.getPrefixedUserName())
            .withSecretKey(broker.getRepositorySecret());

        return new BucketWipeOperations(new S3JerseyClient(s3Config));
    }
}
