package com.emc.ecs.management.sdk;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.NamespaceAction;
import com.emc.ecs.management.sdk.model.NamespaceUpdate;

public class NamespaceActionTest extends EcsActionTest {

    @Before
    public void setUp() throws EcsManagementClientException {
	connection.login();
    }

    @After
    public void cleanup() throws EcsManagementClientException {
	connection.logout();
    }

    @Test
    public void namespaceDoesNotExistTest()
	    throws EcsManagementClientException {
	assertFalse(NamespaceAction.exists(connection, namespace));
    }

    @Test
    public void createExistsAndDeleteNamespaceTest()
	    throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	assertFalse(NamespaceAction.exists(connection, namespace));
	NamespaceAction.create(connection, namespace, "noadmin",
		replicationGroup);
	assertTrue(NamespaceAction.exists(connection, namespace));
	NamespaceAction.delete(connection, namespace);
	assertFalse(NamespaceAction.exists(connection, namespace));
    }

    @Test
    public void getNamespaceTest() throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	NamespaceAction.create(connection, namespace, "noadmin",
		replicationGroup);
	assertTrue(NamespaceAction.get(connection, namespace).getName()
		.equals(namespace));
	NamespaceAction.delete(connection, namespace);
    }

    @Test
    public void updateNamespaceTest() throws EcsManagementClientException {
	NamespaceAction.create(connection, namespace, "noadmin",
		replicationGroup);
	assertFalse(NamespaceAction.get(connection, namespace)
		.getIsEncryptionEnabled());
	NamespaceUpdate update = new NamespaceUpdate();
	update.setIsEncryptionEnabled(true);
	NamespaceAction.update(connection, namespace, update);
	assertTrue(NamespaceAction.get(connection, namespace)
		.getIsEncryptionEnabled());
    }
}