package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.service.S3Service;
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

    public static final String FILENAME_PREFIX = "service-instance";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private S3Service s3;

    private static String getFilename(String id) {
        return FILENAME_PREFIX + "/" + id + ".json";
    }

    @PostConstruct
    public void initialize() throws URISyntaxException {
        logger.info("Service instance file prefix: {}", FILENAME_PREFIX);
    }

    public void save(ServiceInstance instance) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        objectMapper.writeValue(output, instance);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        String filename = getFilename(instance.getServiceInstanceId());

        logger.info("Saving instance to repository as {}", filename);

        s3.putObject(filename, input);
    }

    public ServiceInstance find(String id) throws IOException {
        String filename = getFilename(id);
        logger.debug("Loading service instance from repository file {}", filename);
        GetObjectResult<InputStream> input = s3.getObject(filename);
        return objectMapper.readValue(input.getObject(), ServiceInstance.class);
    }

    public void delete(String id) {
        String filename = getFilename(id);
        logger.info("Deleting repository file {}", filename);
        s3.deleteObject(filename);
    }
}