package com.emc.ecs.management.sdk;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.model.NamespaceQuotaParam;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.NamespaceQuotaAction;
import com.emc.ecs.management.sdk.model.NamespaceQuotaDetails;

public class NamespaceQuotaActionTest extends EcsActionTest {

    @Before
    public void setUp() throws EcsManagementClientException {
	connection.login();
    }

    @After
    public void cleanup() throws EcsManagementClientException {
	connection.logout();
    }

    @Test
    public void testApplyRemoveNamespaceQuota()
	    throws EcsManagementClientException {
	NamespaceQuotaAction.create(connection, namespace,
		new NamespaceQuotaParam(namespace, 10, 8));
	NamespaceQuotaDetails quota = NamespaceQuotaAction.get(connection,
		namespace);
	assertEquals(10, quota.getBlockSize());
	assertEquals(8, quota.getNotificationSize());
	NamespaceQuotaAction.delete(connection, namespace);
	quota = NamespaceQuotaAction.get(connection, namespace);
	assertEquals(-1, quota.getBlockSize());
	assertEquals(-1, quota.getNotificationSize());
    }

}