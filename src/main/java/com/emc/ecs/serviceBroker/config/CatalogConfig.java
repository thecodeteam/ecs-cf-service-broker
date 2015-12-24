package com.emc.ecs.serviceBroker.config;

import java.util.List;
import java.util.stream.Collectors;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxy;

@ConfigurationProperties(prefix = "catalog")
@Configuration
public class CatalogConfig {
	
	private List<ServiceDefinitionProxy> services;

	public CatalogConfig() {
		super();
	}
	
	@Bean
	Catalog catalog() {
		return new Catalog(services.stream()
				.map(s -> s.unproxy())
				.collect(Collectors.toList()));
	}
	
	public CatalogConfig(List<ServiceDefinitionProxy> services) {
		super();
		this.services = services;
	}

	public List<ServiceDefinitionProxy> getServices() {
		return services;
	}
	
	public void setServices(List<ServiceDefinitionProxy> services) {
		this.services = services;
	}

}