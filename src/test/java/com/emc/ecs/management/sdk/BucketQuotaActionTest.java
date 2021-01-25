package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.BucketQuotaDetails;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BucketQuotaActionTest extends EcsActionTest {
    private String bucket = "testbucket3";

    @Before
    public void setUp() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        connection.login();
        BucketAction.create(connection, bucket, namespace, replicationGroupID);
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
        BucketQuotaAction.create(connection, namespace, bucket, 10, 8);
        BucketQuotaDetails quotaDetails = BucketQuotaAction.get(connection,
                namespace, bucket);
        assertEquals(10, quotaDetails.getBlockSize());
        assertEquals(8, quotaDetails.getNotificationSize());
        BucketQuotaAction.delete(connection, namespace, bucket);
        quotaDetails = BucketQuotaAction.get(connection, namespace, bucket);
        assertEquals(-1, quotaDetails.getBlockSize());
        assertEquals(-1, quotaDetails.getNotificationSize());
    }
}
