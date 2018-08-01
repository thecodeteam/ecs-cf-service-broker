package com.emc.ecs.cloudfoundry.broker.config;

import com.emc.ecs.cloudfoundry.broker.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@ConfigurationProperties(prefix = "catalog")
public class CatalogConfig {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<ServiceDefinitionProxy> services;
    private Map<Integer, List<PlanProxy>> plans = new HashMap<>();
    private Map<Integer, Map<String, Object>> settings = new HashMap<>();

    public CatalogConfig() {
        super();
    }

    public CatalogConfig(List<ServiceDefinitionProxy> services) {
        super();
        this.services = services;
    }

    @Bean
    public Catalog catalog() {
        return new Catalog(mergeServices().stream()
                .filter(ServiceDefinitionProxy::getActive)
                .map(ServiceDefinitionProxy::unproxy)
                .collect(Collectors.toList()));
    }

    public List<ServiceDefinitionProxy> mergeServices() {
        return IntStream.range(0, services.size()).mapToObj(index -> {
            ServiceDefinitionProxy s = services.get(index);
            if (plans.containsKey(index))
                s.setPlans(plans.get(index));
            if (settings.containsKey(index))
                s.setServiceSettings(settings.get(index));
            return s;
        }).collect(Collectors.toList());
    }

    public List<ServiceDefinitionProxy> getServices() {
        return this.services;
    }

    public void setPlanCollection0(String planCollectionJson) throws IOException {
        this.plans.put(0, parsePlanCollection(planCollectionJson));
    }

    public void setPlanCollection1(String planCollectionJson) throws IOException {
        this.plans.put(1, parsePlanCollection(planCollectionJson));
    }

    public void setPlanCollection2(String planCollectionJson) throws IOException {
        this.plans.put(2, parsePlanCollection(planCollectionJson));
    }

    public void setPlanCollection3(String planCollectionJson) throws IOException {
        this.plans.put(3, parsePlanCollection(planCollectionJson));
    }

    public void setPlanCollection4(String planCollectionJson) throws IOException {
        this.plans.put(4, parsePlanCollection(planCollectionJson));
    }

    private List<PlanProxy> parsePlanCollection(String planCollectionJson) throws IOException {
        List<PlanCollectionInstance> thesePlans =
                objectMapper.readValue(planCollectionJson, new TypeReference<List<PlanCollectionInstance>>() {
                });
        return thesePlans.stream().map(p -> {
            PlanMetadataProxy planMetadata = new PlanMetadataProxy(p.getBullets(), p.getCosts());
            PlanProxy plan = new PlanProxy(p.getGuid(), p.getName(), p.getDescription(), planMetadata, p.getFree(), p.getServiceSettings());
            plan.setRepositoryPlan(p.getRepositoryPlan());
            return plan;
        }).collect(Collectors.toList());
    }

    public void setServiceSettings0(String serviceSettingsJson) throws IOException {
        this.settings.put(0, parseServiceSettings(serviceSettingsJson));
    }

    public void setServiceSettings1(String serviceSettingsJson) throws IOException {
        this.settings.put(1, parseServiceSettings(serviceSettingsJson));
    }

    public void setServiceSettings2(String serviceSettingsJson) throws IOException {
        this.settings.put(2, parseServiceSettings(serviceSettingsJson));
    }

    public void setServiceSettings3(String serviceSettingsJson) throws IOException {
        this.settings.put(3, parseServiceSettings(serviceSettingsJson));
    }

    public void setServiceSettings4(String serviceSettingsJson) throws IOException {
        this.settings.put(4, parseServiceSettings(serviceSettingsJson));
    }

    private Map<String, Object> parseServiceSettings(String settingsJson) throws IOException {
        TileSelector selector = objectMapper.readValue(settingsJson, TileSelector.class);
        Map<String, Object> settings = selector.getSelectedOption();

        if (settings.containsKey("service_type")) {
            settings.put("service-type", settings.get("service_type"));
            settings.remove("service_type");
        }

        if (settings.containsKey("head_type")) {
            settings.put("head-type", settings.get("head_type"));
            settings.remove("head_type");
        }

        if (settings.containsKey("access_during_outage")) {
            settings.put("access-during-outage", settings.get("access_during_outage"));
            settings.remove("file_accessible");
        }

        if (settings.containsKey("file_accessible")) {
            settings.put("file-accessible", settings.get("file_accessible"));
            settings.remove("file_accessible");
        }

        if (settings.containsKey("default_retention")) {
            settings.put("default-retention", settings.get("default_retention"));
            settings.remove("default_retention");
        }

        if (settings.containsKey("compliance_enabled")) {
            settings.put("compliance-enabled", settings.get("compliance_enabled"));
            settings.remove("compliance_enabled");
        }

        if (settings.containsKey("default_bucket_quota")) {
            settings.put("default-bucket-quota", settings.get("default_bucket_quota"));
            settings.remove("default_bucket_quota");
        }

        return settings;
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