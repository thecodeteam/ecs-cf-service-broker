package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;

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
    public void testNamespaceDoesNotExist()
	    throws EcsManagementClientException {
	assertFalse(NamespaceAction.exists(connection, namespace));
    }

    @Test
    public void createExistsAndDeleteNamespace()
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
    public void testGetNamespace() throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	NamespaceAction.create(connection, namespace, "noadmin", replicationGroup);
	assertTrue(NamespaceAction.get(connection, namespace).getName()
		.equals(namespace));
	NamespaceAction.delete(connection, namespace);
    }

}