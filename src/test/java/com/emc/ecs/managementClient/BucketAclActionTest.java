package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.managementClient.model.BucketAcl;
import com.emc.ecs.managementClient.model.BucketUserAcl;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;

public class BucketAclActionTest extends EcsActionTest {
	private String bucket = "testbucket4";
	private String user = "testuser3";

	@Before
	public void setUp() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		connection.login();
		BucketAction.create(connection, bucket, namespace, replicationGroup);
		ObjectUserAction.create(connection, user, namespace);
	}

	@After
	public void cleanup() throws EcsManagementClientException {
		ObjectUserAction.delete(connection, user);
		BucketAction.delete(connection, bucket, namespace);
		connection.logout();
	}

	@Test
	public void testApplyCheckRemoveBucketUserAcl()
			throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		BucketAcl acl = BucketAclAction.get(connection, bucket, namespace);
		List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
		userAcl.add(new BucketUserAcl(user, Arrays.asList("full_control")));
		acl.getAcl().setUserAccessList(userAcl);
		BucketAclAction.update(connection, bucket, acl);
		BucketAcl bucketAcl = BucketAclAction.get(connection, bucket,
				namespace);
		long userAclCount = bucketAcl.getAcl().getUserAccessList().stream()
				.filter(userAcl1 -> userAcl1.getUser().equals(user)).count();
		assertTrue(userAclCount == 1);
	}
}
