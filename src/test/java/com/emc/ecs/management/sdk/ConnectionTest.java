package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.common.EcsActionTest;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

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