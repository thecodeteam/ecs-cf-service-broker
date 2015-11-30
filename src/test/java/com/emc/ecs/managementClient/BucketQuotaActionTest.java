package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.model.ObjectBucketInfo;

public class BucketQuotaActionTest extends EcsActionTest {
	private String bucket = "testbucket3";
	
	@Before
	public void setUp() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		connection.login();
		BucketAction.create(connection, bucket, namespace, replicationGroup);
	}
	
	@After
	public void cleanup() throws EcsManagementClientException {
		BucketAction.delete(connection, bucket, namespace);
		connection.logout();
	}
	
	@Test
	public void testApplyRemoveBucketQuota() throws EcsManagementClientException, EcsManagementResourceNotFoundException {		
		BucketQuotaAction.create(connection, bucket, namespace, 10, 8);
		ObjectBucketInfo bucketInfo = BucketAction.get(connection, bucket, namespace);
		assertTrue(bucketInfo.getBlockSize() == 10);
		assertTrue(bucketInfo.getNotificationSize() == 8);
		BucketQuotaAction.delete(connection, bucket, namespace);
		bucketInfo = BucketAction.get(connection, bucket, namespace);
		assertTrue(bucketInfo.getBlockSize() == -1);
		assertTrue(bucketInfo.getNotificationSize() == -1);
	}
}
