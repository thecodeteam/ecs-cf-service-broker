package com.emc.ecs.management.sdk;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.BucketQuotaDetails;

public class BucketQuotaActionTest extends EcsActionTest {
    private String bucket = "testbucket3";

    @Before
    public void setUp() throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	connection.login();
	BucketAction.create(connection, bucket, namespace, replicationGroup);
    }

    @After
    public void cleanup() throws EcsManagementClientException {
	BucketAction.delete(connection, bucket, namespace);
	connection.logout();
    }

    @Test
    public void testApplyRemoveBucketQuota()
	    throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	BucketQuotaAction.create(connection, bucket, namespace, 10, 8);
	BucketQuotaDetails quotaDetails = BucketQuotaAction.get(connection,
		bucket, namespace);
	assertEquals(10, quotaDetails.getBlockSize());
	assertEquals(8, quotaDetails.getNotificationSize());
	BucketQuotaAction.delete(connection, bucket, namespace);
	quotaDetails = BucketQuotaAction.get(connection, bucket, namespace);
	assertEquals(-1, quotaDetails.getBlockSize());
	assertEquals(-1, quotaDetails.getNotificationSize());
    }
}
