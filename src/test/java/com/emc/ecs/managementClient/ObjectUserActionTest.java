package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class ObjectUserActionTest extends EcsActionTest {
	private String user = "testuser1";

	@Before
	public void setUp() throws EcsManagementClientException {
		connection.login();
	}

	@After
	public void cleanup() throws EcsManagementClientException {
		connection.logout();
	}

	@Test
	public void testUserDoesNotExist() throws EcsManagementClientException {
		assertFalse(ObjectUserAction.exists(connection, user, namespace));
	}

	@Test
	public void createExistsAndDeleteObjectUser()
			throws EcsManagementClientException {
		ObjectUserAction.create(connection, user, namespace);
		assertTrue(ObjectUserAction.exists(connection, user, namespace));
		ObjectUserAction.delete(connection, user);
		assertFalse(ObjectUserAction.exists(connection, user, namespace));
	}

}