package com.emc.ecs.serviceBroker.config;

import com.emc.ecs.serviceBroker.EcsManagementClient;

public class EcsConfig {
	private EcsManagementClient ecsClient;
	private String bucketName;
	private String repoUser;
	private String repoSecret;
	
	public EcsConfig(EcsManagementClient ecsClient, String bucketName, String repoUser, String repoSecret) {
		super();
		this.ecsClient = ecsClient;
		this.bucketName = bucketName;
		this.repoUser = repoUser;
		this.repoSecret = repoSecret;
	}

	public EcsManagementClient getEcsClient() {
		return ecsClient;
	}
	public void setEcsClient(EcsManagementClient ecsClient) {
		this.ecsClient = ecsClient;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getRepoUser() {
		return repoUser;
	}

	public String getRepoSecret() {
		return repoSecret;
	}
}