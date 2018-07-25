package com.emc.ecs.cloudfoundry.broker.config;

import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import org.codehaus.jackson.annotate.JsonValue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.*;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "catalog")
@Configuration
public class CatalogConfig {
    private boolean enableCatalogServices;
    private String description;
    private String displayName;



    private List<ServiceDefinitionProxy> services;

    public CatalogConfig() {
        super();
    }

    public CatalogConfig(List<ServiceDefinitionProxy> services) {
        super();
        this.services = services;
    }

    @Bean
    public Catalog catalog() {
        return new Catalog(services.stream().map(ServiceDefinitionProxy::unproxy)
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
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("Unable to find configured service id: " + serviceId));
    }

    public void setCatalogConfigs(String catalog){
        try {
            JSONObject catalogValues = new JSONObject().getJSONObject(catalog);
            this.description = catalogValues.getString("");
            this.displayName = catalogValues.getString("");
            this.enableCatalogServices = catalogValues.getBoolean("");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}