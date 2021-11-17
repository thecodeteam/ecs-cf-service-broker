package com.emc.ecs.servicebroker.model;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.catalog.DashboardClient;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private Boolean active;
    private Boolean bindable;
    private List<String> tags;
    private Boolean repositoryService = false;

    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, Object> serviceSettings = new HashMap<>();
    private List<PlanProxy> plans = new ArrayList<>();
    private List<String> requires = new ArrayList<>();
    private Boolean planUpdatable = true;
    private DashboardClientProxy dashboardClient;
    private Boolean instancesRetrievable;
    private Boolean bindingsRetrievable;

    public Boolean getInstancesRetrievable() {
        return instancesRetrievable;
    }

    public void setInstancesRetrievable(Boolean instancesRetrievable) {
        this.instancesRetrievable = instancesRetrievable;
    }

    public Boolean getBindingsRetrievable() {
        return bindingsRetrievable;
    }

    public void setBindingsRetrievable(Boolean bindingsRetrievable) {
        this.bindingsRetrievable = bindingsRetrievable;
    }

    public Boolean getAllowContextUpdates() {
        return allowContextUpdates;
    }

    public void setAllowContextUpdates(Boolean allowContextUpdates) {
        this.allowContextUpdates = allowContextUpdates;
    }

    private Boolean allowContextUpdates;

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

       return  new ServiceDefinition(id, name, description, bindable, planUpdatable,
                instancesRetrievable, bindingsRetrievable, allowContextUpdates, realPlans, tags, metadata, requires,
                realDashboardClient);
    }

    public Boolean getActive() {
        if (active == null)
            return true;
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public Boolean getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(Boolean repositoryService) {
        this.repositoryService = repositoryService;
    }

    public PlanProxy getRepositoryPlan() {
        return plans.stream().filter(PlanProxy::getRepositoryPlan)
                .findFirst()
                .orElseThrow(() -> new ServiceBrokerException("At least one plan must be configured as a 'repository-plan"));
    }

    @Override
    public String toString() {
        return "ServiceDefinitionProxy{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", serviceSettings=" + serviceSettings +
                '}';
    }
}