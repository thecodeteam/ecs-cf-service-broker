package com.emc.ecs.servicebroker;

import com.emc.ecs.management.sdk.EcsManagementAPIConnection;
import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.controller.RepositoryListController;
import com.emc.ecs.servicebroker.controller.RestartController;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.service.EcsService;
import com.emc.ecs.servicebroker.service.EcsServiceInstanceBindingService;
import com.emc.ecs.servicebroker.service.EcsServiceInstanceService;
import com.emc.ecs.servicebroker.service.s3.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

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
    public ManagementAPIConnection managementAPIConnection() {
        // TODO create objectscale profile after workflows implementation
        if (OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            return createObjectscaleConnection();
        } else {
            return createEcsConnection();
        }
    }

    private ManagementAPIConnection createObjectscaleConnection() {
        return null;
    }

    private EcsManagementAPIConnection createEcsConnection() {
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

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion(broker.getBrokerApiVersion());
    }

    @Bean
    public EcsService ecsService() {
        return new EcsService();
    }

    @Bean
    @DependsOn("ecsService")
    public S3Service s3Service() {
        return new S3Service();
    }

    @Bean
    public ServiceInstanceBindingService ecsServiceInstanceBindingService() throws EcsManagementClientException {
        if (OBJECTSCALE.equalsIgnoreCase(broker.getApiType())) {
            return new EcsServiceInstanceBindingService();
        } else {
            return new EcsServiceInstanceBindingService();
        }

    }

    @Bean
    public ServiceInstanceRepository serviceInstanceRepository() {
        return new ServiceInstanceRepository();
    }

    @Bean
    public ServiceInstanceService ecsServiceInstanceService() throws EcsManagementClientException {
        return new EcsServiceInstanceService();
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
