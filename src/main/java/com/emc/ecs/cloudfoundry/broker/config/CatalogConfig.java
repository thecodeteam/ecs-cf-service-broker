package com.emc.ecs.cloudfoundry.broker.config;

import com.emc.ecs.cloudfoundry.broker.model.PlanCollectionInstance;
import com.emc.ecs.cloudfoundry.broker.model.PlanMetadataProxy;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ConfigurationProperties(prefix = "catalog")
@Configuration
public class CatalogConfig {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<ServiceDefinitionProxy> services;
    private List<List<PlanProxy>> plans;

    public CatalogConfig() {
        super();
    }

    public CatalogConfig(List<ServiceDefinitionProxy> services) {
        super();
        this.services = services;
    }

    @Bean
    public Catalog catalog() {
        return new Catalog(IntStream.range(0, 5)
                .mapToObj(index -> {
                    ServiceDefinitionProxy s = services.get(index);
                    s.setPlans(plans.get(index));
                    return s;
                })
                .filter(ServiceDefinitionProxy::getActive)
                .map(ServiceDefinitionProxy::unproxy)
                .collect(Collectors.toList()));
    }

    public List<ServiceDefinitionProxy> getServices() {
        return services;
    }

    public void setPlanCollection0(String planCollectionJson) throws IOException {
        parsePlanCollection(planCollectionJson, 0);
    }

    public void setPlanCollection1(String planCollectionJson) throws IOException {
        parsePlanCollection(planCollectionJson, 1);
    }

    public void setPlanCollection2(String planCollectionJson) throws IOException {
        parsePlanCollection(planCollectionJson, 2);
    }

    public void setPlanCollection3(String planCollectionJson) throws IOException {
        parsePlanCollection(planCollectionJson, 3);
    }

    public void setPlanCollection4(String planCollectionJson) throws IOException {
        parsePlanCollection(planCollectionJson, 4);
    }

    private void parsePlanCollection(String planCollectionJson, int i) throws IOException {
        List<PlanCollectionInstance> thesePlans =
                objectMapper.readValue(planCollectionJson, new TypeReference<List<PlanCollectionInstance>>(){});
        this.plans.set(i, thesePlans.stream().map(p -> {
            PlanMetadataProxy planMetadata = new PlanMetadataProxy(p.getBullets(), p.getCosts());
            PlanProxy plan = new PlanProxy(p.getGuid(), p.getName(), p.getDescription(), planMetadata, p.getFree(), p.getServiceSettings());
            plan.setRepositoryPlan(p.getRepositoryPlan());
            return plan;
        }).collect(Collectors.toList()));
    }

    public void setServices(List<ServiceDefinitionProxy> services) {
        this.services = services;
    }


    public ServiceDefinitionProxy findServiceDefinition(String serviceId) {
        return services.stream().filter(s -> s.getId().equals(serviceId))
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("Unable to find configured service id: " + serviceId));
    }

    public ServiceDefinitionProxy getRepositoryService() {
        return services.stream().filter(ServiceDefinitionProxy::getRepositoryService)
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("At least one service must be configured as a 'repository-service"));
    }
}