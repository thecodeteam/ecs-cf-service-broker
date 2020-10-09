package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private BrokerConfig broker;

    private S3Client s3;

    private String bucket;

    @PostConstruct
    public void initialize() throws URISyntaxException {
        String repositoryEndpoint = broker.getRepositoryEndpoint();

        bucket = broker.getPrefixedBucketName();

        logger.info("Initializing client for S3 endpoint: '{}', bucket '{}'", repositoryEndpoint, bucket);

        S3Config s3Config = new S3Config(new URI(repositoryEndpoint));

        s3Config.withIdentity(broker.getPrefixedUserName());

        logger.info("S3 config {}", s3Config);

        s3Config.withSecretKey(broker.getRepositorySecret());

        this.s3 = new S3JerseyClient(s3Config, new URLConnectionClientHandler());

        logger.info("Testing access to S3 endpoint {} - checking existence of {}", repositoryEndpoint, this.bucket);

        if (s3.bucketExists(this.bucket)) {
            logger.info("Test OK. Bucket {} exists", this.bucket);
            // TODO verify access to objects inside bucket
        } else {
            logger.info("Test OK. Bucket {} doesnt exist yet", this.bucket);
        }
    }

    public void putObject(String filename, Object content) {
        s3.putObject(bucket, filename, content, null);
    }

    public GetObjectResult<InputStream> getObject(String filename) {
        return s3.getObject(bucket, filename);
    }

    public void deleteObject(String filename) {
        s3.deleteObject(bucket, filename);
    }
}
