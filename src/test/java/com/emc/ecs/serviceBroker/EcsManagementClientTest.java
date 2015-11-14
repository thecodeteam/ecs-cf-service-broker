package com.emc.ecs.serviceBroker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.emc.ecs.serviceBroker.model.BucketAcl;
import com.emc.ecs.serviceBroker.model.DataServiceReplicationGroup;
import com.emc.ecs.serviceBroker.model.UserSecretKey;

public class EcsManagementClientTest {
	
	private EcsManagementClient ecs = new EcsManagementClient("https://146.148.65.187:4443", "root", "ChangeMe", "ns1", "rg1");
	
	@Test
	public void testLogin() throws EcsManagementClientException {
		assertFalse(ecs.isLoggedIn());
		ecs.login();
		assertTrue(ecs.isLoggedIn());
		assertNotNull(ecs.getAuthToken());
	}

	@Test
	public void testUserDoesNotExist() throws EcsManagementClientException {
		assertFalse(ecs.userExists("testuser1"));
	}
	
	@Test
	public void createExistsAndDeleteObjectUser() throws EcsManagementClientException {
		ecs.createObjectUser("testuser1");
		assertTrue(ecs.userExists("testuser1"));
		ecs.deleteObjectUser("testuser1");
		assertFalse(ecs.userExists("testuser1"));
	}
	
	@Test
	public void createUserSecretKey() throws EcsManagementClientException {
		ecs.createObjectUser("testuser2");
		UserSecretKey secret = ecs.createUserSecretKey("testuser2");
		assertNotNull(secret.getSecretKey());
		ecs.deleteObjectUser("testuser2");
	}
	
	@Test
	public void testBucketDoesNotExist() throws EcsManagementClientException {
		assertFalse(ecs.bucketExists("testbucket1"));
	}
	
	@Test
	public void listReplicationGroups() throws EcsManagementClientException {
		List<DataServiceReplicationGroup> rgList = ecs.listReplicationGroups();
		assertTrue(rgList.size() == 1);
	}
	
	@Test
	public void getReplicationGroup() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		DataServiceReplicationGroup rg = ecs.getReplicationGroup("rg1");
		assertTrue(rg.getName().equals("rg1"));
		assertTrue(rg.getId().equals("urn:storageos:ReplicationGroupInfo:3b3a2648-f513-4f5c-b1ad-00fb3afe5b90:global"));
	}
	
	@Test
	public void getReplicationGroupId() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		assertTrue(ecs.getReplicationGroupId().equals("urn:storageos:ReplicationGroupInfo:3b3a2648-f513-4f5c-b1ad-00fb3afe5b90:global"));
	}
	
	@Test
	public void createExistsAndDeleteBucket() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		ecs.createBucket("testbucket2");
		assertTrue(ecs.bucketExists("testbucket2"));
		ecs.deleteBucket("testbucket2");
		assertFalse(ecs.bucketExists("testbucket2"));
	}
	
	@Test
	public void testGetBucket() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		ecs.createBucket("testbucket3");
		assertTrue(ecs.getBucket("testbucket3").getName().equals("testbucket3"));
		ecs.deleteBucket("testbucket3");
	}
	
	@Test
	public void testApplyCheckRemoveBucketUserAcl() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		ecs.createBucket("testbucket4");
		ecs.createObjectUser("spiegela");
		ecs.applyBucketUserAcl("testbucket4", "spiegela", "full_control");
		BucketAcl acl = ecs.getBucketAcl("testbucket4");
		long userAclCount = acl
				.getAcl()
				.getUserAccessList()
				.stream()
				.filter(userAcl -> userAcl.getUser().equals("spiegela"))
				.count(); 
		assertTrue(userAclCount == 1);
		ecs.deleteBucket("testbucket4");
		ecs.deleteObjectUser("spiegela");
	}
}