package com.emc.ecs.management.sdk;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.NamespaceRetentionAction;
import com.emc.ecs.management.sdk.model.RetentionClassCreate;
import com.emc.ecs.management.sdk.model.RetentionClassDetails;
import com.emc.ecs.management.sdk.model.RetentionClassUpdate;

public class NamespaceRetentionActionTest extends EcsActionTest {

    private static final String RETENTION_CLASS = "rc1";

    @Before
    public void setUp() throws EcsManagementClientException {
	connection.login();
    }

    @After
    public void cleanup() throws EcsManagementClientException {
	connection.logout();
    }

    @Test
    public void testCreateExistsRemoveNamespaceRetentionClass()
	    throws EcsManagementClientException {
	assertFalse(NamespaceRetentionAction.exists(connection, namespace,
		RETENTION_CLASS));
	NamespaceRetentionAction.create(connection, namespace,
		new RetentionClassCreate(RETENTION_CLASS, 60));
	assertTrue(NamespaceRetentionAction.exists(connection, namespace,
		RETENTION_CLASS));
	NamespaceRetentionAction.delete(connection, namespace, RETENTION_CLASS);
	assertFalse(NamespaceRetentionAction.exists(connection, namespace,
		RETENTION_CLASS));
    }

    @Test
    public void testUpdateNamespaceRetentionClass()
	    throws EcsManagementClientException {
	NamespaceRetentionAction.create(connection, namespace,
		new RetentionClassCreate(RETENTION_CLASS, 60));
	RetentionClassDetails retention = NamespaceRetentionAction
		.get(connection, namespace, RETENTION_CLASS);
	assertEquals(RETENTION_CLASS, retention.getName());
	assertEquals(60, retention.getPeriod());
	NamespaceRetentionAction.update(connection, namespace, RETENTION_CLASS,
		new RetentionClassUpdate(120));
    }
}
