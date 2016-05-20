package com.emc.ecs.serviceBroker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static com.emc.ecs.common.Fixtures.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.managementClient.BucketAction;
import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.managementClient.NamespaceAction;
import com.emc.ecs.managementClient.ObjectUserAction;
import com.emc.ecs.managementClient.ObjectUserSecretAction;
import com.emc.ecs.managementClient.ReplicationGroupAction;
import com.emc.ecs.managementClient.model.DataServiceReplicationGroup;
import com.emc.ecs.managementClient.model.NamespaceCreate;
import com.emc.ecs.managementClient.model.NamespaceUpdate;
import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.serviceBroker.config.BrokerConfig;
import com.emc.ecs.serviceBroker.config.CatalogConfig;

@RunWith(PowerMockRunner.class)
public class EcsServiceTest {
    
    @Mock
    private Connection connection;

    @Mock
    private BrokerConfig broker;

    @Mock
    private CatalogConfig catalog;
    
    @Autowired
    @InjectMocks
    EcsService ecs;
    
    @PrepareForTest({ ReplicationGroupAction.class, BucketAction.class,
	    ObjectUserAction.class, ObjectUserSecretAction.class })
    @Test
    public void initializeStaticConfigTest()
	    throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	PowerMockito.mockStatic(ReplicationGroupAction.class);

	when(broker.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
	when(broker.getPrefix()).thenReturn(PREFIX);
	when(broker.getReplicationGroup()).thenReturn(RG_NAME);
	when(broker.getNamespace()).thenReturn(NAMESPACE);
	when(broker.getRepositoryUser()).thenReturn("user");
	when(broker.getRepositoryBucket()).thenReturn("repository");

	DataServiceReplicationGroup rg = new DataServiceReplicationGroup();
	rg.setName(RG_NAME);
	rg.setId(RG_ID);
	PowerMockito.mockStatic(BucketAction.class);
	when(BucketAction.exists(connection, REPO_BUCKET, NAMESPACE))
		.thenReturn(true);
	
	PowerMockito.mockStatic(ReplicationGroupAction.class);
	when(ReplicationGroupAction.list(connection))
		.thenReturn(Arrays.asList(rg));

	PowerMockito.mockStatic(ObjectUserAction.class);
	when(ObjectUserAction.exists(connection, REPO_USER, NAMESPACE))
		.thenReturn(true);
	
	UserSecretKey secretKey = new UserSecretKey();
	secretKey.setSecretKey(TEST);
	PowerMockito.mockStatic(ObjectUserSecretAction.class);
	when(ObjectUserSecretAction.list(connection, REPO_USER))
		.thenReturn(Arrays.asList(secretKey));

	ecs.initialize();

	assertEquals(OBJ_ENDPOINT, ecs.getObjectEndpoint());
	assertEquals(PREFIX + "test", ecs.prefix(TEST));
	verify(broker, times(1)).setRepositoryEndpoint(OBJ_ENDPOINT);
	verify(broker, times(1)).setRepositorySecret(TEST);
    }

    @PrepareForTest({NamespaceAction.class})
    @Test
    public void createNamespaceDefaultTest()
	    throws Exception {
	Map<String, Object> params = new HashMap<>();
	NamespaceCreate createParam = new NamespaceCreate(NAMESPACE, RG_ID, params);
	PowerMockito.mockStatic(NamespaceAction.class);
	PowerMockito.doNothing().when(NamespaceAction.class, "create", connection, createParam);

	when(catalog.findServiceDefinition(SERVICE_ID))
	.thenReturn(namespaceServiceFixture());
	
	ecs.createNamespace(NAMESPACE, SERVICE_ID, PLAN_ID1, params);
	
	Mockito.verify(catalog, times(1)).findServiceDefinition(SERVICE_ID);
	PowerMockito.verifyStatic();
    }

    @PrepareForTest({NamespaceAction.class}) 
    @Test
    public void changeNamespacePlanTest() throws Exception {
	PowerMockito.mockStatic(NamespaceAction.class);
	Map<String, Object> params = new HashMap<>();
	NamespaceUpdate updateParam = new NamespaceUpdate(params);
	PowerMockito.mockStatic(NamespaceAction.class);
	PowerMockito.doNothing().when(NamespaceAction.class, "update",
		connection, NAMESPACE, updateParam);

	when(catalog.findServiceDefinition(SERVICE_ID))
	.thenReturn(namespaceServiceFixture());

	ecs.changeNamespacePlan(NAMESPACE, SERVICE_ID, PLAN_ID2, params);

	Mockito.verify(catalog, times(1)).findServiceDefinition(SERVICE_ID);
	PowerMockito.verifyStatic();
    }

    @PrepareForTest({NamespaceAction.class})
    @Test
    public void createNamespaceWithParamsTest() throws Exception {
	Map<String, Object> params = new HashMap<>();
	params.put("domain-group-admins", "group1@foo.com");
	params.put("encrypted", true);
	params.put("compliance-enabled", true);
	params.put("access-during-outage", true);
	params.put("default-bucket-quota", 10);
	NamespaceCreate createParam = new NamespaceCreate(NAMESPACE, RG_ID, params);
	PowerMockito.mockStatic(NamespaceAction.class);
	PowerMockito.doNothing().when(NamespaceAction.class, "create", connection, createParam);

	when(catalog.findServiceDefinition(SERVICE_ID))
		.thenReturn(namespaceServiceFixture());

	ecs.createNamespace(NAMESPACE, SERVICE_ID, PLAN_ID1, params);

	Mockito.verify(catalog, times(1)).findServiceDefinition(SERVICE_ID);
	PowerMockito.verifyStatic();
    }

    @PrepareForTest({NamespaceAction.class})
    @Test
    public void changeNamespacePlanWithParamsTest() throws Exception {
	Map<String, Object> params = new HashMap<>();
	params.put("domain-group-admins", "group1@foo.com");
	params.put("encrypted", true);
	params.put("compliance-enabled", true);
	params.put("access-during-outage", true);
	params.put("default-bucket-quota", 10);
	NamespaceUpdate updateParam = new NamespaceUpdate(params);
	PowerMockito.mockStatic(NamespaceAction.class);
	PowerMockito.doNothing().when(NamespaceAction.class, "update",
		connection, NAMESPACE, updateParam);

	when(catalog.findServiceDefinition(SERVICE_ID))
		.thenReturn(namespaceServiceFixture());

	ecs.changeNamespacePlan(NAMESPACE, SERVICE_ID, PLAN_ID1, params);

	Mockito.verify(catalog, times(1)).findServiceDefinition(SERVICE_ID);
	PowerMockito.verifyStatic();
    }

}
