package com.emc.ecs.servicebroker.service.s3;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.model.Constants;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.*;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.emc.object.s3.request.ListObjectsRequest;
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

        String userName = broker.getPrefixedUserName();

        logger.info("Initializing client for S3 endpoint: '{}', bucket '{}', repository username '{}'", repositoryEndpoint, bucket, userName);

        S3Config s3Config = new S3Config(new URI(repositoryEndpoint));

        s3Config.withIdentity(userName);

        logger.info("S3 config {}", s3Config);

        s3Config.withSecretKey(broker.getRepositorySecret());

        this.s3 = new S3JerseyClient(s3Config, new URLConnectionClientHandler());

        logger.info("Testing access to S3 endpoint {}", repositoryEndpoint);

        if (s3.bucketExists(this.bucket)) {
            logger.debug("Test OK. Bucket {} exists", this.bucket);
            ListObjectsResult listObjectsResult = s3.listObjects(new ListObjectsRequest(this.bucket).withMaxKeys(3));
            listObjectsResult.getObjects().forEach(
                    s3Object -> {
                        logger.debug("Testing access to '{}'", s3Object.getKey());
                        AccessControlList objectAcl = s3.getObjectAcl(bucket, s3Object.getKey());
                        CanonicalUser owner = objectAcl.getOwner();
                        String objectOwner = owner.getDisplayName();
                        if (userName.equalsIgnoreCase(objectOwner)) {
                            String errorMessage = String.format(
                                    "S3 Object owners differ in repository, check repository username in broker settings: current username is '%s', found object owner '%s' on '%s'",
                                    userName, objectOwner, s3Object.getKey());
                            logger.warn(errorMessage);
                        }
                    }
            );
        } else {
            logger.info("Test OK. Bucket {} doesnt exist yet", this.bucket);
        }
    }

    public void putObject(String filename, Object content) {
        s3.putObject(bucket, filename, content, "application/json");
    }

    public GetObjectResult<InputStream> getObject(String filename) {
        return s3.getObject(bucket, filename);
    }

    public void deleteObject(String filename) {
        s3.deleteObject(bucket, filename);
    }

    public ListObjectsResult listObjects() {
        return s3.listObjects(bucket);
    }

    public ListObjectsResult listObjects(String prefix, String marker, int pageSize) {
        ListObjectsRequest request = new ListObjectsRequest(bucket);
        if (marker != null) {
            request.setMarker(marker);
        }
        request.setPrefix(prefix);
        if (pageSize != 0) {
            request.setMaxKeys(pageSize);
        }
        return s3.listObjects(request);
    }
}
