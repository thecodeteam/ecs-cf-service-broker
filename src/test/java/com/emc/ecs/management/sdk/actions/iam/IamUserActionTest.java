package com.emc.ecs.management.sdk.actions.iam;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.actions.ObjectUserAction;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IamUserActionTest extends EcsActionTest {
    private String user = "testuser1";

    /*
    @Test
    public void testUserDoesNotExist() throws EcsManagementClientException {
        assertFalse(ObjectUserAction.exists(connection, user, namespace));
    }
     */

    @Test
    public void createUser() throws EcsManagementClientException {
        IAMUserAction.create(connection, user, namespace);
//        assertTrue(ObjectUserAction.exists(connection, user, namespace));

        IAMAccessKeyAction.create(connection, user, namespace);

//        ObjectUserAction.delete(connection, user);
//        assertFalse(ObjectUserAction.exists(connection, user, namespace));
    }

}