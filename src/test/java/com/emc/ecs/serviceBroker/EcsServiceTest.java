package com.emc.ecs.serviceBroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emc.ecs.managementClient.model.NamespaceInfo;
import com.emc.ecs.serviceBroker.config.Application;
import com.github.tomakehurst.wiremock.client.WireMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
	initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("development")
public class EcsServiceTest {
    @Autowired
    EcsService ecs;
     
    private static String NAMESPACE = "testns1";
    private static String SERVICE_ID = "09cac1c6-1b0a-11e6-b6ba-3e1d05defe78";
    private static String PLAN_ID = "09cac5b8-1b0a-11e6-b6ba-3e1d05defe78";

    @Test
    public void initializeTest() {
	assertEquals(ecs.getObjectEndpoint(), "http://127.0.0.1:9020");
	assertEquals(ecs.prefix("test"), "ecs-cf-broker-test");
    }

    @Test
    public void namespaceCreateExistsDeleteTest()
	    throws EcsManagementClientException {
	assertFalse(ecs.namespaceExists(NAMESPACE));
	ecs.createNamespace(NAMESPACE, SERVICE_ID, PLAN_ID, new HashMap<>());
	assertTrue(ecs.namespaceExists(NAMESPACE));
	ecs.deleteNamespace(NAMESPACE);
	assertFalse(ecs.namespaceExists(NAMESPACE));
    }
    
    @Test
    public void changeNamespacePlanTest() throws EcsManagementClientException {
	ecs.createNamespace(NAMESPACE, SERVICE_ID, PLAN_ID, new HashMap<>());
	NamespaceInfo info = ecs.getNamespaceInfo(NAMESPACE);
	assertFalse(info.getIsEncryptionEnabled());
	HashMap<String, Object> params = new HashMap<>();
	params.put("encrypted", true);
	ecs.changeNamespacePlan(NAMESPACE, SERVICE_ID, PLAN_ID, params);
	info = ecs.getNamespaceInfo(NAMESPACE);
	assertTrue(info.getIsEncryptionEnabled());
	ecs.deleteNamespace(NAMESPACE);
    }
}
