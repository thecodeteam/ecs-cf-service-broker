package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.BucketPolicy;
import com.emc.ecs.management.sdk.model.BucketPolicyStatement;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.emc.ecs.servicebroker.model.Constants.BUCKET_POLICY_VERSION;
import static org.junit.Assert.*;

public class BucketPolicyActionTest extends EcsActionTest {
    private String bucket = "testbucket4";
    private String user = "testuser3";

    @Before
    public void setUp() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        BucketAction.create(connection, bucket, namespace, replicationGroupID);
        ObjectUserAction.create(connection, user, namespace);
    }

    @After
    public void cleanup() throws EcsManagementClientException {
        ObjectUserAction.delete(connection, user);
        BucketAction.delete(connection, bucket, namespace);
    }

    @Test
    public void testApplyCheckRemoveBucketPolicy()
            throws EcsManagementClientException {
        assertFalse(BucketPolicyAction.exists(connection, bucket, namespace));

        BucketPolicyStatement statement = new BucketPolicyStatement();
        statement.setBucketPolicyEffect("Allow");
        statement.setPrincipal(user);
        statement.setBucketPolicyAction(Collections.singletonList("s3:*"));
        statement.setBucketPolicyResource(Collections.singletonList(bucket));
        BucketPolicy policy = new BucketPolicy(BUCKET_POLICY_VERSION, bucket, Collections.singletonList(statement));

        BucketPolicyAction.update(connection, bucket, policy, namespace);

        assertTrue(BucketPolicyAction.exists(connection, bucket, namespace));

        BucketPolicy policy2 = BucketPolicyAction.get(connection, bucket, namespace);
        assertEquals(policy.getVersion(), policy2.getVersion());
        assertEquals(1, policy2.getBucketPolicyStatements().size());
        assertEquals(policy.getBucketPolicyStatements().get(0).getBucketPolicyEffect(), policy2.getBucketPolicyStatements().get(0).getBucketPolicyEffect());
        assertEquals(policy.getBucketPolicyStatements().get(0).getPrincipal(), policy2.getBucketPolicyStatements().get(0).getPrincipal());
        assertEquals(policy.getBucketPolicyStatements().get(0).getBucketPolicyAction(), policy2.getBucketPolicyStatements().get(0).getBucketPolicyAction());
        assertEquals(policy.getBucketPolicyStatements().get(0).getBucketPolicyResource(), policy2.getBucketPolicyStatements().get(0).getBucketPolicyResource());

        BucketPolicyAction.remove(connection, bucket, namespace);

        assertFalse(BucketPolicyAction.exists(connection, bucket, namespace));
    }
}
