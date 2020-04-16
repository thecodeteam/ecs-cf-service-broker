package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BucketActionTest extends EcsActionTest {
    private String bucket = "testbucket2";

    @Before
    public void setUp() throws EcsManagementClientException {
        connection.login();
    }

    @After
    public void cleanup() throws EcsManagementClientException {
        connection.logout();
    }

    @Test
    public void testBucketDoesNotExist() throws EcsManagementClientException {
        assertFalse(BucketAction.exists(connection, bucket, namespace));
    }

    @Test
    public void createExistsAndDeleteBucket()
            throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        assertFalse(BucketAction.exists(connection, bucket, namespace));
        BucketAction.create(connection, bucket, namespace, replicationGroupID);
        assertTrue(BucketAction.exists(connection, bucket, namespace));
        BucketAction.delete(connection, bucket, namespace);
        assertFalse(BucketAction.exists(connection, bucket, namespace));
    }

    @Test
    public void testGetBucket() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        BucketAction.create(connection, bucket, namespace, replicationGroupID);
        assertTrue(BucketAction.get(connection, bucket, namespace).getName()
                .equals(bucket));
        BucketAction.delete(connection, bucket, namespace);
    }

}