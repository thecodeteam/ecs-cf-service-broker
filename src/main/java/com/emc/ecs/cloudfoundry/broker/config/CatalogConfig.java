package com.emc.ecs.cloudfoundry.broker.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;

@ConfigurationProperties(prefix = "catalog")
@Configuration
public class CatalogConfig {

	private List<ServiceDefinitionProxy> services;

	public CatalogConfig() {
		super();
	}

	public CatalogConfig(List<ServiceDefinitionProxy> services) {
		super();
		this.services = services;
	}

	@Bean
	Catalog catalog() {
		return new Catalog(services.stream()
				.map(s -> s.unproxy())
				.collect(Collectors.toList()));
	}

	public List<ServiceDefinitionProxy> getServices() {
		return services;
	}

	public void setServices(List<ServiceDefinitionProxy> services) {
		this.services = services;
	}

	public ServiceDefinitionProxy findServiceDefinition(String serviceId) {
		return services.stream().filter(s -> s.getId().equals(serviceId))
				.findFirst().get();
	}
}