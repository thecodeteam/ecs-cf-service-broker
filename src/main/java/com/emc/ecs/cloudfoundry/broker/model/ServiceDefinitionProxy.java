package com.emc.ecs.cloudfoundry.broker.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.DashboardClient;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Component
public class ServiceDefinitionProxy {
    private String id;
    private String name;
    private String description;
    private String type;
    private Boolean active;
    private Boolean bindable;
    private Boolean planUpdatable = true;
    private List<String> tags;
    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, Object> serviceSettings = new HashMap<>();
    private List<PlanProxy> plans = new ArrayList<>();
    private List<String> requires = new ArrayList<>();
    private DashboardClientProxy dashboardClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceDefinitionProxy() {
        super();
    }

    public ServiceDefinitionProxy(String id, String name, String description,
            Boolean bindable, Boolean planUpdatable, List<String> tags,
            Map<String, Object> serviceSettings, Map<String, Object> metadata,
            List<PlanProxy> plans, List<String> requires,
            DashboardClientProxy dashboardClient) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.bindable = bindable;
        this.planUpdatable = planUpdatable;
        this.tags = tags;
        this.serviceSettings = serviceSettings;
        this.metadata = metadata;
        this.plans = plans;
        this.requires = requires;
        this.dashboardClient = dashboardClient;
    }

    public ServiceDefinition unproxy() {
        List<Plan> realPlans = null;
        if (plans != null)
            realPlans = plans.stream().map(PlanProxy::unproxy)
                    .collect(Collectors.toList());

        DashboardClient realDashboardClient = null;
        if (dashboardClient != null)
            realDashboardClient = dashboardClient.unproxy();

        if ((boolean) this.getServiceSettings().getOrDefault("file-accessible", false))
            requires.add("volume_mount");

        return new ServiceDefinition(id, name, description, bindable,
                planUpdatable, realPlans, tags, metadata, requires,
                realDashboardClient);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setPlanCollection(String planCollectionJson) throws IOException {
        List<PlanCollectionInstance> plans =
                objectMapper.readValue(planCollectionJson, new TypeReference<List<PlanCollectionInstance>>(){});
        this.plans = plans.stream().map(p -> {
            List<String> bullets =
                    Stream.of(p.getBullet1(), p.getBullet2(), p.getBullet3(), p.getBullet4(), p.getBullet5())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            Map<String, Object> costMap = new HashMap<>();
            costMap.put("usd", p.getCostUSD());
            List<CostProxy> costs = Collections.singletonList(new CostProxy(costMap, p.getCostUnit()));

            PlanMetadataProxy planMetadata = new PlanMetadataProxy(bullets, costs);

            Map<String, Object> serviceSettings = new HashMap<>();
            if (p.getAccessDuringOutage() != null)
                serviceSettings.put("access-during-outage", p.getAccessDuringOutage());

            if (p.getDefaultRetention() != null)
                serviceSettings.put("default-retention", p.getAccessDuringOutage());

            if (p.getQuotaLimit() != null || p.getQuotaWarn() != null) {
                Map quota = new HashMap<String, Object>();
                if (p.getQuotaLimit() != null)
                    quota.put("limit", p.getQuotaLimit());

                if (p.getQuotaWarn() != null)
                    quota.put("warn",  p.getQuotaWarn());
                serviceSettings.put("quota", quota);
            }

            return new PlanProxy(p.getGuid(), p.getName(), p.getDescription(), planMetadata, p.getFree(), serviceSettings);
        }).collect(Collectors.toList());
    }

    public void setServiceTypeSelector(String serviceTypeJson) throws IOException {
        TileSelector tileSelector = objectMapper.readValue(serviceTypeJson, TileSelector.class);


        Map<String, Object> option = tileSelector.getOption();
        option.put("service-type", tileSelector.getValue());

        this.serviceSettings = option;
    }

    public void setServiceDetailsSelector(String serviceTypeJson) throws IOException {
        TileSelector tileSelector = objectMapper.readValue(serviceTypeJson, TileSelector.class);

        Map<String, Object> option = tileSelector.getOption();
        option.put("metadata", tileSelector.getValue());

        this.metadata = option;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getBindable() {
        return bindable;
    }

    public void setBindable(Boolean bindable) {
        this.bindable = bindable;
    }

    public Boolean getPlanUpdatable() {
        return planUpdatable;
    }

    public void setPlanUpdatable(Boolean planUpdatable) {
        this.planUpdatable = planUpdatable;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<PlanProxy> getPlans() {
        return plans;
    }

    public void setPlans(List<PlanProxy> plans) {
        this.plans = plans;
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    public DashboardClientProxy getDashboardClient() {
        return dashboardClient;
    }

    public void setDashboardClient(DashboardClientProxy dashboardClient) {
        this.dashboardClient = dashboardClient;
    }

    public PlanProxy findPlan(String planId) {
        return plans.stream().filter(p -> p.getId().equals(planId)).findFirst()
                .orElseThrow(() -> new ServiceBrokerException("Unable to find configured plan ID: " + planId));
    }

    public Map<String, Object> getServiceSettings() {
        return serviceSettings;
    }

    public void setServiceSettings(Map<String, Object> serviceSettings) {
        this.serviceSettings = serviceSettings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}