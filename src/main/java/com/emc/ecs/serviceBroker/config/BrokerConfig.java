package com.emc.ecs.serviceBroker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "broker")
public class BrokerConfig {

	// TODO support multiple Cloud Foundry instances per
	// http://docs.cloudfoundry.org/services/supporting-multiple-cf-instances.html
	// TODO support syslog drain URL

	private String managementEndpoint;
	private String namespace;
	private String replicationGroup;
	private String repositorySecret;
	private String repositoryEndpoint;
	private String repositoryUser = "user";
	private String username = "root";
	private String password = "ChangeMe";
	private String repositoryBucket = "repository";
	private String prefix = "ecs-cf-broker-";
	private String brokerApiVersion = "2.8";

	public BrokerConfig() {
		super();
	}

	public String getManagementEndpoint() {
		return managementEndpoint;
	}

	public void setManagementEndpoint(String managementEndpoint) {
		this.managementEndpoint = managementEndpoint;
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

	public String getRepositoryUser() {
		return repositoryUser;
	}

	public void setRepositoryUser(String repositoryUser) {
		this.repositoryUser = repositoryUser;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRepositoryBucket() {
		return repositoryBucket;
	}

	public void setRepositoryBucket(String repositoryBucket) {
		this.repositoryBucket = repositoryBucket;
	}

	public String getRepositoryEndpoint() {
		return repositoryEndpoint;
	}

	public void setRepositoryEndpoint(String repositoryEndpoint) {
		this.repositoryEndpoint = repositoryEndpoint;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getBrokerApiVersion() {
		return brokerApiVersion;
	}

	public void setBrokerApiVersion(String brokerApiVersion) {
		this.brokerApiVersion = brokerApiVersion;
	}

	public String getRepositorySecret() {
		return repositorySecret;
	}

	public void setRepositorySecret(String repositorySecret) {
		this.repositorySecret = repositorySecret;
	}

	public String getPrefixedBucketName() {
		return prefix + repositoryBucket;
	}

	public String getPrefixedUserName() {
		return prefix + repositoryUser;
	}
}