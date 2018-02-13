package com.emc.ecs.cloudfoundry.broker.model;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.DashboardClient;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Component
public class ServiceDefinitionProxy {
    private String id;
    private String name;
    private String description;
    private String type;
    private Boolean bindable;
    private Boolean planUpdatable;
    private List<String> tags;
    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, Object> serviceSettings = new HashMap<>();
    private List<PlanProxy> plans;
    private List<String> requires;
    private DashboardClientProxy dashboardClient;

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

        return new ServiceDefinition(id, name, description, bindable,
                planUpdatable, realPlans, tags, metadata, requires,
                realDashboardClient);
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