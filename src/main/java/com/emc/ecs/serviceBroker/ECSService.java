package com.emc.ecs.serviceBroker;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;

import com.emc.ecs.serviceBroker.model.UserSecretKey;

public class ECSService {
	
	private EcsManagementClient ecs;

	public EcsManagementClient getEcs() {
		return ecs;
	}

	public void setEcs(EcsManagementClient ecs) {
		this.ecs = ecs;
	}

	public Object getObjectEndpoint() {
		return null;
	}

	public Map<String, String> getBucketInfo(String id) {
		return ecs.getBucket(id).toMap();
	}

	public void deleteBucket(String id) {
		ecs.deleteBucket(id);
	}

	public void createBucket(String id, String planId) {
		if (planId == "ecs-bucket-small" || planId == "ecs-bucket-unlimited") {
			ecs.createBucket(id);
		}
		if (planId == "ecs-bucket-small") {
			ecs.applyBucketQuota(id, 10, 8);
		}
	}
	
	public void changeBucketPlan(String id, String planId) {
		if (planId == "ecs-bucket-small") {
			ecs.applyBucketQuota(id, 10, 8);
		}
		if (planId == "ecs-bucket-unlimited") {
			ecs.removeBucketQuota(id);
		}
	}

	public UserSecretKey createUser(String username) throws ServiceBrokerException {
		try {
			ecs.createObjectUser(username);
			return ecs.createUserSecretKey(username);
		}
		catch(EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	public Boolean userExists(String username) throws ServiceBrokerException {
		try {
			return ecs.userExists(username);
		}
		catch(EcsManagementClientException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
	}

	public void deleteUser(String username) throws EcsManagementClientException {
		ecs.deleteObjectUser(username);
	}

	public void addUserToBucket(String bucket, String username) {
		ecs.applyBucketUserAcl(bucket, username, "FULL_CONTROL");
	}

	public void removeUserFromBucket(String bucket, String username) {
		ecs.removeBucketUserAcl(bucket, username);
	}

	public boolean bucketExists(String id) {
		return ecs.bucketExists(id);
	}
	
}