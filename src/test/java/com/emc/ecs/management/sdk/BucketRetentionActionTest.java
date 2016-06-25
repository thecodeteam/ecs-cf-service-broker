package com.emc.ecs.management.sdk;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.DefaultBucketRetention;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BucketRetentionActionTest extends EcsActionTest {

    private final int THIRTY_DAYS_IN_SEC = 2592000;
    private final String BUCKET = "testbucket5";

    @Before
    public void setUp() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        connection.login();
        BucketAction.create(connection, BUCKET, namespace, replicationGroup);
    }

    @After
    public void cleanup() throws EcsManagementClientException {
        BucketAction.delete(connection, BUCKET, namespace);
        connection.logout();
    }

    @Test
    public void getUpdateBucketRetention() throws Exception {
        DefaultBucketRetention retention = BucketRetentionAction.get(connection, namespace, BUCKET);
        assertEquals(-1, retention.getPeriod());
        BucketRetentionAction.update(connection, namespace, BUCKET, THIRTY_DAYS_IN_SEC);
        DefaultBucketRetention retention2 = BucketRetentionAction.get(connection, namespace, BUCKET);
        assertEquals(THIRTY_DAYS_IN_SEC, retention2.getPeriod());
    }
}