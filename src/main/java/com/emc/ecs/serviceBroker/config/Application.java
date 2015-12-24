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

@EnableAutoConfiguration
@ComponentScan
public class Application {
	
	@Autowired
	private BrokerConfig broker;
	
	@Autowired
	private CatalogConfig catalog;

	public Application() {
		super();
	}

	@Bean
	public Connection ecsConnection() {
		URL certificate = getClass().getClassLoader()
				.getResource("localhost.pem");
		return new Connection(broker.getManagementEndpoint(),
				broker.getUsername(), broker.getPassword(), certificate);
	}
	
	@Bean
	public EcsService ecsService() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		return new EcsService(ecsConnection(), broker, catalog);
	}
	
	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion(broker.getBrokerApiVersion());
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public BrokerConfig getBroker() {
		return broker;
	}

	public void setBroker(BrokerConfig broker) {
		this.broker = broker;
	}
   	
}