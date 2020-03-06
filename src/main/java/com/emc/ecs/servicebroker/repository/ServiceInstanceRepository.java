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

import static java.lang.String.format;

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
        logger.info(format("Creating S3 config with repository endpoint %s", broker.getRepositoryEndpoint()));

        S3Config s3Config = new S3Config(
                new URI(broker.getRepositoryEndpoint()));

        logger.info(format("Created S3 config %s", s3Config));

        s3Config.withIdentity(broker.getPrefixedUserName())
                .withSecretKey(broker.getRepositorySecret());
        this.s3 = new S3JerseyClient(s3Config,
        		new URLConnectionClientHandler());

        logger.info(format("JerseyClient S3 config %s", this.s3.getS3Config()));

        this.bucket = broker.getPrefixedBucketName();
    }

    public void save(ServiceInstance instance) throws IOException {
        logger.info(format("Host: %s, Namespace: %s, Protocol: %s, Identity: %s, Port: %s",
                this.s3.getS3Config().getHost(),
                this.s3.getS3Config().getNamespace(),
                this.s3.getS3Config().getProtocol(),
                this.s3.getS3Config().getIdentity(),
                this.s3.getS3Config().getPort()));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        objectMapper.writeValue(output, instance);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        logger.info(format("Saving Repository - bucket: %s", bucket));
        s3.putObject(bucket, getFilename(instance.getServiceInstanceId()),
                input, null);
    }

    public ServiceInstance find(String id) throws IOException {
        GetObjectResult<InputStream> input = s3.getObject(bucket,
                getFilename(id));
        return objectMapper.readValue(input.getObject(), ServiceInstance.class);
    }

    public void delete(String id) {
        s3.deleteObject(bucket, getFilename(id));
    }

}