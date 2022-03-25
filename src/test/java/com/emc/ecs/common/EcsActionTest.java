package com.emc.ecs.common;

import com.emc.ecs.management.sdk.EcsManagementAPIConnection;
import com.emc.ecs.servicebroker.config.Application;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;

import static com.emc.ecs.common.Fixtures.RG_ID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
public abstract class EcsActionTest {

    @Autowired
    private BrokerConfig broker;

    protected EcsManagementAPIConnection connection;

    protected String namespace;
    protected String replicationGroupID = RG_ID;

    @SuppressWarnings("unused")
    @PostConstruct
    protected void init() {
        connection = new EcsManagementAPIConnection(
                broker.getManagementEndpoint(), broker.getUsername(), broker.getPassword(), broker.getCertificate(), false
        );

        namespace = broker.getNamespace();
    }
}