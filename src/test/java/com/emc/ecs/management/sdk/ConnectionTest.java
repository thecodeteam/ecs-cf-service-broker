package com.emc.ecs.management.sdk;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.common.EcsActionTest;

public class ConnectionTest extends EcsActionTest {

    @After
    public void cleanup() throws EcsManagementClientException {
	connection.logout();
    }

    @Test
    public void testLogin() throws EcsManagementClientException {
	assertFalse(connection.isLoggedIn());
	connection.login();
	assertTrue(connection.isLoggedIn());
	assertNotNull(connection.getAuthToken());
    }

}