package com.emc.ecs.serviceBroker.config;

import java.net.URL;

import org.cloudfoundry.community.servicebroker.model.BrokerApiVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.serviceBroker.repository.EcsRepositoryCredentials;

@Configuration
@ComponentScan(basePackages = "com.emc.ecs.serviceBroker")
public class BrokerConfig {
	
	@Value("${endpoint}")
	private String endpoint;
	
	@Value("${port}")
	private String port;
	
	@Value("${username}")
	private String username;
	
	@Value("${password}")
	private String password;
	
	@Value("${replicationGroup}")
	private String replicationGroup;
	
	@Value("${namespace}")
	private String namespace;
	
	@Value("${repositoryUser}")
	private String repositoryUser;
	
	@Value("${repositoryBucket}")
	private String repositoryBucket;
	

	@Bean
	public Connection ecsConnection() {
		URL certificate = getClass().getClassLoader().getResource("localhost.pem");
		return new Connection("https://" + endpoint + ":" + port, username, password, certificate);
	}
	
	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion("2.7");
	}
	
	@Bean
	public EcsRepositoryCredentials getRepositoryCredentials() {
		return new EcsRepositoryCredentials(repositoryBucket, repositoryUser, namespace, replicationGroup);
	}
}