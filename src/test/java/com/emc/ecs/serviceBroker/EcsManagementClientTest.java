package com.emc.ecs.serviceBroker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.nitorcreations.junit.runners.NestedRunner;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.emc.ecs.serviceBroker.model.BucketAcl;
import com.emc.ecs.serviceBroker.model.DataServiceReplicationGroup;
import com.emc.ecs.serviceBroker.model.ObjectBucketInfo;
import com.emc.ecs.serviceBroker.model.UserSecretKey;

@RunWith(NestedRunner.class)
public class EcsManagementClientTest {
	
	private EcsManagementClient ecs = new EcsManagementClient("https://146.148.65.187:4443",
			"root", "ChangeMe", "ns1", "rg1");
	
	@After
	public void cleanup() throws EcsManagementClientException {
		ecs.logout();
	}
	
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
	
	public class WhenUserExists {
		private String user = "testuser2";
		
		@Before
		public void setUp() throws EcsManagementClientException {
			ecs.createObjectUser(user);			
		}
		
		@After
		public void cleanup() throws EcsManagementClientException {
			ecs.deleteObjectUser(user);
		}
		
		@Test
		public void createUserSecretKey() throws EcsManagementClientException {
			UserSecretKey secret = ecs.createUserSecretKey(user);
			assertNotNull(secret.getSecretKey());
		}
	}
	
	public class WhenBucketExists {
		private String bucket = "testbucket3";
		
		@Before
		public void setUp() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
			ecs.createBucket(bucket);
		}
		
		@After
		public void cleanup() throws EcsManagementClientException {
			ecs.deleteBucket(bucket);	
		}
		
		@Test
		public void testGetBucket() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
			assertTrue(ecs.getBucket(bucket).getName().equals(bucket));
		}

		@Test
		public void testApplyRemoveBucketQuota() throws EcsManagementClientException, EcsManagementResourceNotFoundException {		
			ecs.applyBucketQuota(bucket, 10, 8);
			ObjectBucketInfo bucketInfo = ecs.getBucket(bucket);
			assertTrue(bucketInfo.getBlockSize() == 10);
			assertTrue(bucketInfo.getNotificationSize() == 8);
			ecs.removeBucketQuota(bucket);
			bucketInfo = ecs.getBucket(bucket);
			assertTrue(bucketInfo.getBlockSize() == -1);
			assertTrue(bucketInfo.getNotificationSize() == -1);
		}
	}
	
	public class WhenUserAndBucketExist {
		private String bucket = "testbucket4";
		private String user = "testuser3";
		
		@Before
		public void setUp() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
			ecs.createBucket(bucket);
			ecs.createObjectUser(user);
		}
		
		@After
		public void cleanup() throws EcsManagementClientException {
			ecs.deleteObjectUser(user);
			ecs.deleteBucket(bucket);
		}
		
		@Test
		public void testApplyCheckRemoveBucketUserAcl() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
			ecs.applyBucketUserAcl(bucket, user, "full_control");
			BucketAcl acl = ecs.getBucketAcl(bucket);
			long userAclCount = acl
					.getAcl()
					.getUserAccessList()
					.stream()
					.filter(userAcl -> userAcl.getUser().equals(user))
					.count(); 
			assertTrue(userAclCount == 1);
		}
	}
}