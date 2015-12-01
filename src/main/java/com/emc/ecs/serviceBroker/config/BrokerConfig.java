package com.emc.ecs.serviceBroker.config;

import java.net.URL;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.serviceBroker.repository.EcsRepositoryCredentials;

@Configuration
@ComponentScan(basePackages = "com.emc.ecs.serviceBroker")
public class BrokerConfig {

	@Bean
	public Connection ecsConnection() {
		URL certificate = getClass().getClassLoader().getResource("localhost.pem");
		return new Connection("https://8.34.215.78:4443", "root", "ChangeMe", certificate);
	}
	
	@Bean
	public EcsRepositoryCredentials getRepositoryCredentials() {
		return new EcsRepositoryCredentials("ecs-cf-service-broker-repository", "ecs-cf-service-broker-repository",
				"ns1", "urn:storageos:ReplicationGroupInfo:d4fc7068-1051-49ee-841f-1102e44c841e:global");
	}

}