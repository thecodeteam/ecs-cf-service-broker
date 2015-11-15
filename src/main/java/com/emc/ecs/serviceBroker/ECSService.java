package com.emc.ecs.serviceBroker;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.ecs.serviceBroker.config.EcsConfig;
import com.emc.ecs.serviceBroker.model.BaseUrlInfo;
import com.emc.ecs.serviceBroker.model.ObjectBucketInfo;
import com.emc.ecs.serviceBroker.model.UserSecretKey;

@Service
public class ECSService {
	
	private EcsManagementClient ecs;
	private String bucketName;
	private String repoUser;
	private String repoSecret;

	@Autowired
	public ECSService(EcsConfig ecsConfig) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		super();
		this.ecs = ecsConfig.getEcsClient();
		this.bucketName = ecsConfig.getBucketName();
		this.repoUser = ecsConfig.getRepoUser();
		this.repoSecret = ecsConfig.getRepoSecret();
		if (! ecs.bucketExists(bucketName)) ecs.createBucket(bucketName);
		if (! ecs.userExists(repoUser)) {
			ecs.createObjectUser(repoUser);
			ecs.createUserSecretKey(repoUser, repoSecret);
			ecs.applyBucketUserAcl(bucketName, repoUser, repoSecret);
		}
	}
	
	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		if (! ecs.bucketExists(bucketName))
			ecs.createBucket(bucketName);
		this.bucketName = bucketName;
	}

	public void setEcs(EcsManagementClient ecs) {
		this.ecs = ecs;
	}
	
	public EcsManagementClient getEcs() {
		return ecs;
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

	public String getObjectEndpoint() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		// with VDC/inactive in the API, we would make a more intelligent selection
		// as it stands, there's not enough info -- just pick the 1st one.
		BaseUrlInfo baseUrlInfo = ecs.getBaseUrlInfo(ecs.listBaseUrls().get(0).getId());
		if (baseUrlInfo.getNamespaceInHost()) {
			return "https://" + ecs.getNamespace() + "." + baseUrlInfo.getBaseurl();
		} else {
			return "https://" + baseUrlInfo.getBaseurl() + "/" + ecs.getNamespace();
		}
	}
}