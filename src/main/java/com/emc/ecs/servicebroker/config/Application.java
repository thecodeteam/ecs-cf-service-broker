package com.emc.ecs.servicebroker.config;

import com.emc.ecs.management.sdk.EcsManagementAPIConnection;
import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.ObjectscaleGatewayConnection;
import com.emc.ecs.management.sdk.ObjectstoreManagementAPIConnection;
import com.emc.ecs.servicebroker.controller.RepositoryListController;
import com.emc.ecs.servicebroker.controller.RestartController;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.service.*;
import com.emc.ecs.servicebroker.service.ObjectstoreService;
import com.emc.ecs.servicebroker.service.ObjectscaleServiceInstanceBindingService;
import com.emc.ecs.servicebroker.service.ObjectscaleServiceInstanceService;
import com.emc.ecs.servicebroker.service.s3.S3Service;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import java.net.URI;
import java.net.URISyntaxException;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECTSCALE;

@SuppressWarnings("unused")
@SpringBootApplication
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static ConfigurableApplicationContext context;

    private static String[] args;

    @Autowired
    private BrokerConfig broker;

    /* start application */
    public static void main(String[] args) {
        setArgs(args);
        Application.context = SpringApplication.run(Application.class, getArgs());
    }

    /* restart application */
    public static void main() {
        Application.context.close();
        Application.context = SpringApplication.run(Application.class, getArgs());
    }

    @Bean
    public RepositoryListController repositoryListController() {
        return new RepositoryListController();
    }

    @Bean
    public RestartController restartController() {
        return new RestartController();
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion(broker.getBrokerApiVersion());
    }

    @Bean(name = "objectscaleGateway")
    @ConditionalOnProperty(name = "broker.apiType", havingValue = "objectscale")
    public ObjectscaleGatewayConnection objectscaleGatewayConnection() {
        // In Objectscale, replication group default ID equals objectstore name
        if (broker.getObjectstoreName() != null) {
            broker.setReplicationGroup(broker.getObjectstoreName());
        }
        // In Objectscale, account ID is used as 'namespace' in all management API calls
        if (broker.getAccountId() != null) {
            broker.setNamespace(broker.getAccountId());
        }

        ObjectscaleGatewayConnection c = new ObjectscaleGatewayConnection(
                broker.getObjectscaleGatewayEndpoint(),
                broker.getUsername(),
                broker.getPassword(),
                broker.getCertificate(),
                broker.getIgnoreSslValidation()
        );

        if (broker.getCertificate() != null) {
            logger.info("Instantiating Objectscale gateway connection with SSL certificate");
        } else {
            logger.info("Instantiating unencrypted Objectscale gateway connection");
        }

        c.login();

        return c;
    }

    @Bean(name = "managementAPI")
    @DependsOn("objectscaleGateway")
    @ConditionalOnProperty(name = "broker.apiType", havingValue = "objectscale")
    public ManagementAPIConnection createObjectstoreConnection(ObjectscaleGatewayConnection gatewayConnection) {
        ObjectstoreManagementAPIConnection c = new ObjectstoreManagementAPIConnection(
                broker.getObjectstoreManagementEndpoint(),
                broker.getCertificate(),
                broker.getIgnoreSslValidation(),
                gatewayConnection
        );

        if (broker.getCertificate() != null) {
            logger.info("Instantiating Objectstore management API connection with SSL certificate");
        } else {
            logger.info("Instantiating unencrypted Objectstore management API connection");
        }

        c.login();

        return c;
    }

    @Bean(name = "managementAPI")
    @ConditionalOnProperty(name = "broker.apiType", havingValue = "ecs", matchIfMissing = true)
    public EcsManagementAPIConnection createEcsConnection() {
        if (broker.getCertificate() != null) {
            logger.info("Instantiating ECS connection with SSL certificate");
        } else {
            logger.info("Instantiating unencrypted ECS connection");
        }

        EcsManagementAPIConnection c = new EcsManagementAPIConnection(
                broker.getManagementEndpoint(),
                broker.getUsername(),
                broker.getPassword(),
                broker.getCertificate(),
                broker.getIgnoreSslValidation()
        );

        if (broker.getLoginSessionLength() > 0) {
            logger.info("Max login session length set to {} minutes", broker.getLoginSessionLength());
            c.setMaxLoginSessionLength(broker.getLoginSessionLength());
        }

        c.login();

        return c;
    }

    @Bean(name = "s3Client")
    @ConditionalOnProperty(name = "broker.apiType", havingValue = "objectscale")
    public S3Client objectstoreS3Client(BrokerConfig config) throws URISyntaxException {
        String bucket = config.getPrefixedBucketName();
        String repositoryEndpoint = config.getObjectstoreS3Endpoint();
        String userName = config.getAccessKey();
        String repositorySecret = config.getSecretKey();
        String accountId = config.getAccountId();

        if (repositorySecret == null || repositorySecret.length() == 0) {
            logger.warn("S3 secret key is empty, S3 repository test is likely to fail!");
        }

        logger.info("Preparing S3 endpoint client: '{}', bucket '{}', account '{}', access key '{}'", repositoryEndpoint, bucket, accountId, userName);

        S3Config s3Config = new S3Config(new URI(repositoryEndpoint))
                .withNamespace(accountId)
                .withIdentity(userName)
                .withSecretKey(repositorySecret);

        logger.info("S3 config: {}", s3Config);

        return new S3JerseyClient(s3Config, new URLConnectionClientHandler());
    }

    @Bean(name = "s3Client")
    @ConditionalOnProperty(name = "broker.apiType", havingValue = "ecs", matchIfMissing = true)
    public S3Client ecsS3Client(BrokerConfig config) throws URISyntaxException {
        String bucket = config.getPrefixedBucketName();
        String repositoryEndpoint = config.getRepositoryEndpoint();
        String userName = config.getPrefixedUserName();
        String repositorySecret = config.getRepositorySecret();

        if (repositorySecret == null || repositorySecret.length() == 0) {
            logger.warn("S3 secret key is empty, S3 repository test is likely to fail!");
        }

        logger.info("Preparing S3 endpoint client: '{}', bucket '{}', repository username '{}'", repositoryEndpoint, bucket, userName);

        S3Config s3Config = new S3Config(new URI(repositoryEndpoint))
                .withIdentity(userName)
                .withSecretKey(repositorySecret);

        logger.info("S3 config: {}", s3Config);

        return new S3JerseyClient(s3Config, new URLConnectionClientHandler());
    }

    @Bean
    public StorageService storageService(BrokerConfig config) {
        if (OBJECTSCALE.equalsIgnoreCase(config.getApiType())) {
            return new ObjectstoreService();
        } else {
            return new EcsService();
        }
    }

    @Bean
    @DependsOn("storageService")
    public S3Service s3Service() {
        return new S3Service();
    }

    @Bean
    public ServiceInstanceService ecsServiceInstanceService() throws EcsManagementClientException {
        if (OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            return new ObjectscaleServiceInstanceService();
        } else {
            return new EcsServiceInstanceService();
        }
    }

    @Bean
    public ServiceInstanceBindingService ecsServiceInstanceBindingService() throws EcsManagementClientException {
        if (OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            return new ObjectscaleServiceInstanceBindingService();
        } else {
            return new EcsServiceInstanceBindingService();
        }
    }

    @Bean
    public ServiceInstanceRepository serviceInstanceRepository() {
        return new ServiceInstanceRepository();
    }

    @Bean
    public ServiceInstanceBindingRepository serviceInstanceBindingRepository() {
        return new ServiceInstanceBindingRepository();
    }

    @Bean
    public BucketWipeFactory bucketWipeFactory() {
        return new BucketWipeFactory();
    }

    private static String[] getArgs() {
        return args;
    }

    private static void setArgs(String[] args) {
        Application.args = args;
    }

    public BrokerConfig getBroker() {
        return broker;
    }

    public void setBroker(BrokerConfig broker) {
        this.broker = broker;
    }
}
