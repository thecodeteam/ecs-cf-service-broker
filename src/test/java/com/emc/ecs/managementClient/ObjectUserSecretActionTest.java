package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.model.UserSecretKey;

public class ObjectUserSecretActionTest extends EcsActionTest {
	private String user = "testuser2";
	
	@Before
	public void setUp() throws EcsManagementClientException {
		connection.login();
		ObjectUserAction.create(connection, user, namespace);			
	}
	
	@After
	public void cleanup() throws EcsManagementClientException {
		ObjectUserAction.delete(connection, user);
		connection.logout();
	}
	
	@Test
	public void createUserSecretKey() throws EcsManagementClientException {
		UserSecretKey secret = ObjectUserSecretAction.create(connection, user);
		assertNotNull(secret.getSecretKey());
	}
	
	@Test
	public void listUserSecretKey() throws EcsManagementClientException {
		UserSecretKey secret = ObjectUserSecretAction.create(connection, user);
		List<UserSecretKey> secretKeys = ObjectUserSecretAction.list(connection, user);
		assertEquals(secret.getSecretKey(), secretKeys.get(0).getSecretKey());
	}
}
