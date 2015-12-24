package com.emc.ecs.serviceBroker.config;

import java.net.URL;

import org.cloudfoundry.community.servicebroker.model.BrokerApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.repository.EcsRepositoryCredentials;

@EnableAutoConfiguration
@ComponentScan
public class Application {
	
	@Autowired
	private BrokerConfig broker;
	
	@Autowired
	private static EcsRepositoryCredentials credentials;
	
	@Autowired
	private static EcsService ecs;

	@Bean
	public Connection ecsConnection() {
		URL certificate = getClass().getClassLoader()
				.getResource("localhost.pem");
		return new Connection(broker.getManagementEndpoint(),
				broker.getUsername(), broker.getPassword(), certificate);
	}
	
	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion(broker.getBrokerApiVersion());
	}
	
	@Bean
	public EcsRepositoryCredentials getRepositoryCredentials() {
		EcsRepositoryCredentials creds = new EcsRepositoryCredentials(
				broker.getRepositoryBucket(), broker.getRepositoryUser(),
				broker.getNamespace(), broker.getReplicationGroup(),
				broker.getPrefix());
		String repoEndpoint = broker.getRepositoryEndpoint();
		if (repoEndpoint != null)
			creds.setEndpoint(repoEndpoint);
		return creds;
	}

	public static void main(String[] args) throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		SpringApplication.run(Application.class, args);
		ecs.prepareRepository();

		if (credentials.getEndpoint() == null)
			credentials.setEndpoint(ecs.getObjectEndpoint());
	}
    	
}
