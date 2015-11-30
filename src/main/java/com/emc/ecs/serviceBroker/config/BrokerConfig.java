package com.emc.ecs.serviceBroker.config;

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
		return new Connection("https://104.197.129.211:4443", "root", "ChangeMe");
	}
	
	@Bean
	public EcsRepositoryCredentials getRepositoryCredentials() {
		return new EcsRepositoryCredentials("ecs-cf-service-broker-repository", "ecs-cf-service-broker-repository", "ns1", "rg1");
	}

}