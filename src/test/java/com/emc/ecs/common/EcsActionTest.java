package com.emc.ecs.common;

import com.emc.ecs.cloudfoundry.broker.config.Application;
import com.emc.ecs.cloudfoundry.broker.config.BrokerConfig;
import com.emc.ecs.management.sdk.Connection;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.net.URL;

import static com.emc.ecs.common.Fixtures.RG_ID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
    initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public abstract class EcsActionTest {
	
    @Autowired
    private BrokerConfig broker;

    protected Connection connection;
    protected String namespace;
    protected String replicationGroupID = RG_ID;

    @SuppressWarnings("unused")
    @PostConstruct
    protected void init() {
        String certificate = broker.getCertificate();
    	    	
        connection = new Connection(broker.getManagementEndpoint(),
                broker.getUsername(), broker.getPassword(), certificate);

        namespace = broker.getNamespace();
    }
}