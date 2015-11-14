package com.emc.ecs.serviceBroker;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;

import com.emc.ecs.serviceBroker.model.ObjectBucketInfo;
import com.emc.ecs.serviceBroker.model.UserSecretKey;

public class ECSService {
	
	private EcsManagementClient ecs;

	public EcsManagementClient getEcs() {
		return ecs;
	}

	public void setEcs(EcsManagementClient ecs) {
		this.ecs = ecs;
	}

	public ObjectBucketInfo getBucketInfo(String id) throws EcsManagementClientException {
		return ecs.getBucket(id);
	}

	public void deleteBucket(String id) throws EcsManagementClientException {
		ecs.deleteBucket(id);
	}

	public void createBucket(String id, String planId) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		if (planId == "ecs-bucket-small" || planId == "ecs-bucket-unlimited") {
			ecs.createBucket(id);
		}
		if (planId == "ecs-bucket-small") {
			ecs.applyBucketQuota(id, 10, 8);
		}
	}
	
	public void changeBucketPlan(String id, String planId) throws EcsManagementClientException {
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

	public void addUserToBucket(String bucket, String username) throws EcsManagementClientException {
		ecs.applyBucketUserAcl(bucket, username, "FULL_CONTROL");
	}

	public boolean bucketExists(String id) throws EcsManagementClientException {
		return ecs.bucketExists(id);
	}

	public Object getObjectEndpoint() {
		// TODO Auto-generated method stub
		return null;
	}
	
}