package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.ObjectBucketCreate;
import com.emc.ecs.management.sdk.model.ObjectBucketInfo;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import org.junit.Test;

import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.ACCESS_DURING_OUTAGE;
import static com.emc.ecs.servicebroker.model.Constants.ADO_READ_ONLY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BucketAdoActionTest extends EcsActionTest {
    private String bucket = "adotestbucket";

    @Test
    public void testAdo() throws EcsManagementClientException {
        Map<String, Object> params = Map.of(ACCESS_DURING_OUTAGE, true, ADO_READ_ONLY, true);

        BucketAction.create(connection, new ObjectBucketCreate(bucket, namespace, replicationGroupID, params));

        ObjectBucketInfo bucketInfo = BucketAction.get(connection, bucket, namespace);
        assertTrue(bucketInfo.getIsTsoReadOnly());
        assertTrue(bucketInfo.getIsStaleAllowed());

        BucketAction.delete(connection, bucket, namespace);
    }

    @Test
    public void testUpdate() throws EcsManagementClientException {
        Map<String, Object> params = Map.of(ACCESS_DURING_OUTAGE, true, ADO_READ_ONLY, true);

        BucketAction.create(connection, new ObjectBucketCreate(bucket, namespace, replicationGroupID, params));

        BucketAdoAction.update(connection, namespace, bucket, false);

        ObjectBucketInfo bucketInfo = BucketAction.get(connection, bucket, namespace);
        assertFalse(bucketInfo.getIsTsoReadOnly());
        assertFalse(bucketInfo.getIsStaleAllowed());

        BucketAdoAction.update(connection, namespace, bucket, true);

        bucketInfo = BucketAction.get(connection, bucket, namespace);
        assertTrue(bucketInfo.getIsTsoReadOnly());
        assertTrue(bucketInfo.getIsStaleAllowed());

        BucketAction.delete(connection, bucket, namespace);
    }
}
