package com.emc.ecs.serviceBroker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "broker")
@ComponentScan(basePackages = "com.emc.ecs.serviceBroker")
public class BrokerConfig {
	
	// TODO support multiple Cloud Foundry instances per http://docs.cloudfoundry.org/services/supporting-multiple-cf-instances.html
	// TODO support syslog drain URL
	
	private String managementEndpoint;
	private String namespace;
	private String replicationGroup;
	private String repositoryUser;
	private String username;
	private String password;
	private String repositoryBucket;
	private String repositoryEndpoint;
	private String prefix;
	private String brokerApiVersion;

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
}