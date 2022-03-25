package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.actions.ObjectUserAction;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.common.EcsActionTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectUserActionTest extends EcsActionTest {
    private String user = "testuser1";

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