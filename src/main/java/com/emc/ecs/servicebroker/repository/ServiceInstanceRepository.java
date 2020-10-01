package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ServiceInstanceRepository {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceRepository.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String bucket;

    private S3JerseyClient s3;

    @Autowired
    private BrokerConfig broker;

    private static String getFilename(String id) {
        return "service-instance/" + id + ".json";
    }

    @PostConstruct
    public void initialize() throws URISyntaxException {
        logger.info("Initializing repository with repository endpoint {}", broker.getRepositoryEndpoint());

        S3Config s3Config = new S3Config(new URI(broker.getRepositoryEndpoint()));

        logger.info("Repository S3 config {}", s3Config);

        s3Config.withIdentity(broker.getPrefixedUserName()).withSecretKey(broker.getRepositorySecret());

        this.s3 = new S3JerseyClient(s3Config, new URLConnectionClientHandler());

        logger.debug("JerseyClient S3 config {}", this.s3.getS3Config());

        this.bucket = broker.getPrefixedBucketName();

        logger.info("Service repository bucket: {}", this.bucket);

        logger.info("Testing access to S3 endpoint {} - checking existence of {}", broker.getRepositoryEndpoint(), this.bucket);
        if (s3.bucketExists(this.bucket)) {
            logger.info("Test OK. Bucket {} exists", this.bucket);
        } else {
            logger.info("Test OK. Bucket {} doesnt exist yet", this.bucket);
        }
    }

    public void save(ServiceInstance instance) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        objectMapper.writeValue(output, instance);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        String filename = getFilename(instance.getServiceInstanceId());

        logger.info("Saving repository to {}:{}", bucket, filename);
        logger.debug("S3 config: Host: {}, Namespace: {}, Protocol: {}, Identity: {}, Port: {}",
                this.s3.getS3Config().getHost(),
                this.s3.getS3Config().getNamespace(),
                this.s3.getS3Config().getProtocol(),
                this.s3.getS3Config().getIdentity(),
                this.s3.getS3Config().getPort());

        s3.putObject(bucket, filename, input, null);
    }

    public ServiceInstance find(String id) throws IOException {
        String filename = getFilename(id);

        logger.debug("Loading service instance from repository file {}:{}", bucket, filename);

        GetObjectResult<InputStream> input = s3.getObject(bucket, filename);

        return objectMapper.readValue(input.getObject(), ServiceInstance.class);
    }

    public void delete(String id) {
        String filename = getFilename(id);
        logger.info("Deleting repository instance {}:{}", bucket, filename);
        s3.deleteObject(bucket, filename);
    }

}