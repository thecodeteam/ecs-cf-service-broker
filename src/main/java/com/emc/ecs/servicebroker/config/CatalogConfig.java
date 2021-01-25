package com.emc.ecs.servicebroker.config;

import com.emc.ecs.servicebroker.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.emc.ecs.servicebroker.model.Constants.*;

@Configuration
@ConfigurationProperties(prefix = "catalog")
public class CatalogConfig {
    private static final Logger logger = LoggerFactory.getLogger(CatalogConfig.class);

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
            if (plans.containsKey(index)) {
                s.setPlans(plans.get(index));
            }
            if (settings.containsKey(index)) {
                s.setServiceSettings(settings.get(index));
            }
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
            // TODO include cost amounts, displayname, and properties
            PlanMetadataProxy planMetadata = new PlanMetadataProxy(p.getBullets(), p.getCosts(), null, null);
            PlanProxy plan = new PlanProxy(p.getGuid(), p.getName(), p.getDescription(), planMetadata, p.getFree(), p.getServiceSettings());
            plan.setRepositoryPlan(p.getRepositoryPlan());
            return plan;
        }).collect(Collectors.toList());
    }

    public void setServiceSettings0(String serviceSettingsJson) throws IOException {
        this.settings.put(0, parseServiceSettings(serviceSettingsJson, 0));
    }

    public void setServiceSettings1(String serviceSettingsJson) throws IOException {
        this.settings.put(1, parseServiceSettings(serviceSettingsJson, 1));
    }

    public void setServiceSettings2(String serviceSettingsJson) throws IOException {
        this.settings.put(2, parseServiceSettings(serviceSettingsJson, 2));
    }

    public void setServiceSettings3(String serviceSettingsJson) throws IOException {
        this.settings.put(3, parseServiceSettings(serviceSettingsJson, 3));
    }

    public void setServiceSettings4(String serviceSettingsJson) throws IOException {
        this.settings.put(4, parseServiceSettings(serviceSettingsJson, 4));
    }

    private Map<String, Object> parseServiceSettings(String settingsJson, int serviceIndex) throws IOException {
        TileSelector selector = objectMapper.readValue(settingsJson, TileSelector.class);
        Map<String, Object> settings = selector.getSelectedOption();

        if (selector.getValue().equals("Bucket")) {
            settings.put(SERVICE_TYPE, ServiceType.BUCKET.getAlias());
        } else if (selector.getValue().equals("Namespace")) {
            settings.put(SERVICE_TYPE, ServiceType.NAMESPACE.getAlias());
        } else {
            throw new ServiceBrokerException("Unable to determine service-type from: " + selector.getValue());
        }

        Map<String, String> tileReplacements = new HashMap<>();

        // TODO use Map.of after java 9+ migration done
        tileReplacements.put("head_type", HEAD_TYPE);
        tileReplacements.put("access_during_outage", ACCESS_DURING_OUTAGE);
        tileReplacements.put("file_accessible", FILE_ACCESSIBLE);
        tileReplacements.put("default_retention", DEFAULT_RETENTION);
        tileReplacements.put("compliance_enabled", COMPLIANCE_ENABLED);
        tileReplacements.put("default_bucket_quota", DEFAULT_BUCKET_QUOTA);
        tileReplacements.put("replication_group", REPLICATION_GROUP);
        tileReplacements.put("base_url", BASE_URL);

        for (Map.Entry<String, String> e : tileReplacements.entrySet()) {
            if (settings.containsKey(e.getKey())) {
                settings.put(e.getValue(), settings.get(e.getKey()));
                settings.remove(e.getKey());
            }
        }

        settings = settings.entrySet().stream()
                .peek(e -> {
                    if (e.getValue() == null) {
                        logger.debug("Removing '{}' from service[{}] settings - null value", e.getKey(), serviceIndex);
                    }
                })
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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