package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class ConnectionTest extends EcsActionTest {

	@After
	public void cleanup() throws EcsManagementClientException {
		connection.logout();
	}

	@Test
	public void testLogin() throws EcsManagementClientException {
		System.out.println(connection.getCertificate());
		assertFalse(connection.isLoggedIn());
		connection.login();
		assertTrue(connection.isLoggedIn());
		assertNotNull(connection.getAuthToken());
	}

}