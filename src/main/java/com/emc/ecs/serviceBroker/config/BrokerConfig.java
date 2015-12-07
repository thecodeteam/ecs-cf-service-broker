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
	
	@Value("${namespace}")
	private String namespace;
	
	@Value("${replicationGroup}")
	private String replicationGroup;
	
	@Value("${repositoryUser:user}")
	private String repositoryUser;
	
	@Value("${managementPort:4443}")
	private String managementPort;
	
	@Value("${username:root}")
	private String username;
	
	@Value("${password:ChangeMe}")
	private String password;
	
	@Value("${repositoryBucket:repository}")
	private String repositoryBucket;
	
	@Value("${prefix:ecs-cf-broker-}")
	private String prefix;
	
	@Value("${brokerApiVersion:2.8}")
	private String brokerApiVersion;

	@Bean
	public Connection ecsConnection() {
		URL certificate = getClass().getClassLoader().getResource("localhost.pem");
		return new Connection("https://" + endpoint + ":" + managementPort, username, password, certificate);
	}
	
	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion(brokerApiVersion);
	}
	
	@Bean
	public EcsRepositoryCredentials getRepositoryCredentials() {
		return new EcsRepositoryCredentials(repositoryBucket, repositoryUser, namespace, replicationGroup, prefix);
	}
}