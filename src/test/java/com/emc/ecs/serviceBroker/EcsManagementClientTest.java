package com.emc.ecs.serviceBroker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.emc.ecs.serviceBroker.model.UserSecretKey;

public class EcsManagementClientTest {
	
	private EcsManagementClient ecs = new EcsManagementClient("https://146.148.65.187:4443", "root", "ChangeMe", "ns1", "rg1");
	
	@Test
	public void testLogin() throws EcsManagementClientException {
		assertFalse(ecs.isLoggedIn());
		ecs.login();
		assertTrue(ecs.isLoggedIn());
		assertNotNull(ecs.getAuthToken());
	}

	@Test
	public void testUserDoesNotExist() throws EcsManagementClientException {
		assertFalse(ecs.userExists("testuser1"));
	}
	
	@Test
	public void createExistsAndDeleteObjectUser() throws EcsManagementClientException {
		ecs.createObjectUser("testuser1");
		assertTrue(ecs.userExists("testuser1"));
		ecs.deleteObjectUser("testuser1");
		assertFalse(ecs.userExists("testuser1"));
	}
	
	@Test
	public void createUserSecretKey() throws EcsManagementClientException {
		ecs.createObjectUser("testuser2");
		UserSecretKey secret = ecs.createUserSecretKey("testuser2");
		assertNotNull(secret.getSecretKey());
		ecs.deleteObjectUser("testuser2");
	}
}