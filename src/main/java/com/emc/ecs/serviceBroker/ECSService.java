package com.emc.ecs.serviceBroker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.ecs.managementClient.BaseUrlAction;
import com.emc.ecs.managementClient.BucketAclAction;
import com.emc.ecs.managementClient.BucketAction;
import com.emc.ecs.managementClient.BucketQuotaAction;
import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.managementClient.ObjectUserAction;
import com.emc.ecs.managementClient.ObjectUserSecretAction;
import com.emc.ecs.serviceBroker.model.BaseUrlInfo;
import com.emc.ecs.serviceBroker.model.BucketAcl;
import com.emc.ecs.serviceBroker.model.BucketUserAcl;
import com.emc.ecs.serviceBroker.model.ObjectBucketInfo;
import com.emc.ecs.serviceBroker.model.UserSecretKey;
import com.emc.ecs.serviceBroker.repository.EcsRepositoryCredentials;

@Service
public class EcsService {
	
	private Connection connection;
	private EcsRepositoryCredentials credentials;

	@Autowired
	public EcsService(Connection connection, EcsRepositoryCredentials creds) throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		super();
		this.connection = connection;
		this.credentials = creds;
		prepareRepository(creds);
	}

	private void prepareRepository(EcsRepositoryCredentials creds)
			throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		String bucketName = creds.getBucketName();
		String userName = creds.getUserName();
		if (! bucketExists(bucketName))
			createBucket(bucketName, "ecs-bucket-unlimited");
		
		if (! userExists(userName)) {
			UserSecretKey secretKey = createUser(userName);
			addUserToBucket(bucketName, userName);
			this.credentials.setUserSecret(secretKey.getSecretKey());
		} else {
			this.credentials.setUserSecret(getUserSecret(userName));
		}
	}
	
	public EcsRepositoryCredentials getCredentials() {
		return credentials;
	}
	
	public void setCredentials(EcsRepositoryCredentials credentials) {
		this.credentials = credentials;
	}

	private String getUserSecret(String userName) {
		return null;
	}

	public ObjectBucketInfo getBucketInfo(String id) throws EcsManagementClientException {
		return BucketAction.get(connection, id, credentials.getNamespace());
	}

	public void deleteBucket(String id) throws EcsManagementClientException {
		BucketAction.delete(connection, id, credentials.getNamespace());
	}

	public void createBucket(String id, String planId) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		if (planId.equals("ecs-bucket-small") || planId.equals("ecs-bucket-unlimited")) {
			BucketAction.create(connection, id, credentials.getNamespace(), credentials.getReplicationGroup());
		} else if (planId.equals("ecs-bucket-small")) {
			BucketQuotaAction.create(connection, id, credentials.getNamespace(), 10, 8);
		} else {
			throw new EcsManagementClientException("No service matching plan id");
		}
	}
	
	public void changeBucketPlan(String id, String planId) throws EcsManagementClientException {
		if (planId.equals("ecs-bucket-small")) {
			BucketQuotaAction.create(connection, id, credentials.getNamespace(), 10, 8);
		} else if (planId.equals("ecs-bucket-unlimited")) {
			BucketQuotaAction.delete(connection, id, credentials.getNamespace());
		} else {
			throw new EcsManagementClientException("No service matching plan id");
		}
	}

	public UserSecretKey createUser(String id) throws EcsManagementClientException {
		ObjectUserAction.create(connection, id, credentials.getNamespace());
		ObjectUserSecretAction.create(connection, id);
		return ObjectUserSecretAction.list(connection, id).get(0);
	}

	public Boolean userExists(String id) throws EcsManagementClientException {
		return ObjectUserAction.exists(connection, id, credentials.getNamespace());
	}

	public void deleteUser(String id) throws EcsManagementClientException {
		ObjectUserAction.delete(connection, id);
	}

	public void addUserToBucket(String id, String username) throws EcsManagementClientException {
		BucketAcl acl = BucketAclAction.get(connection, id, credentials.getNamespace());
		List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
		userAcl.add(new BucketUserAcl(username, "full_control"));
		acl.getAcl().setUserAccessList(userAcl);
		BucketAclAction.update(connection, id, acl);
	}

	public boolean bucketExists(String id) throws EcsManagementClientException {
		return BucketAction.exists(connection, id, credentials.getNamespace());
	}

	public String getObjectEndpoint() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		// with VDC/inactive in the API, we would make a more intelligent selection
		// as it stands, there's not enough info -- just pick the 1st one.
		String id = BaseUrlAction.list(connection).get(0).getId();
		BaseUrlInfo baseUrl = BaseUrlAction.get(connection, id);
		return baseUrl.getNamespaceUrl(credentials.getNamespace());
	}
}