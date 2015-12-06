package com.emc.ecs.serviceBroker.repository;

public class EcsRepositoryCredentials {
	private String bucketName;
	private String userName;
	private String userSecret;
	private String namespace;
	private String replicationGroup;
	private String prefix;
	
	public EcsRepositoryCredentials(String bucketName, String userName, String namespace, String replicationGroup, String prefix) {
		super();
		this.bucketName = bucketName;
		this.userName = userName;
		this.namespace = namespace;
		this.replicationGroup = replicationGroup;
		this.prefix = prefix;
	}
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getReplicationGroup() {
		return replicationGroup;
	}

	public void setReplicationGroup(String replicationGroup) {
		this.replicationGroup = replicationGroup;
	}

	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getBucketName() {
		return bucketName;
	}
	
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public void setUserSecret(String usersecret) {
		this.userSecret = usersecret;
	}
	
	public String getUserSecret() {
		return userSecret;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
