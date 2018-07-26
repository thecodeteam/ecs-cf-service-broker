package com.emc.ecs.cloudfoundry.broker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlanCollectionInstance {
    @JsonProperty("access_during_outage")
    private Boolean accessDuringOutage;

    @JsonProperty("bullet_1")
    private String bullet1;

    @JsonProperty("bullet_2")
    private String bullet2;

    @JsonProperty("bullet_3")
    private String bullet3;

    @JsonProperty("bullet_4")
    private String bullet4;

    @JsonProperty("bullet_5")
    private String bullet5;

    @JsonProperty("cost_unit")
    private String costUnit;

    @JsonProperty("cost_usd")
    private String costUSD;

    @JsonProperty("default_retention")
    private String defaultRetention;

    @JsonProperty("description")
    private String description;

    @JsonProperty("free")
    private Boolean free;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("quota_limit")
    private String quotaLimit;

    @JsonProperty("quota_warn")
    private String quotaWarn;

    @JsonProperty("repository_plan")
    private String repositoryPlan;

    public void setBullet1(String bullet1) {
        this.bullet1 = bullet1;
    }

    public void setBullet2(String bullet2) {
        this.bullet2 = bullet2;
    }

    public void setBullet3(String bullet3) {
        this.bullet3 = bullet3;
    }

    public void setBullet4(String bullet4) {
        this.bullet4 = bullet4;
    }

    public void setBullet5(String bullet5) {
        this.bullet5 = bullet5;
    }

    public List<String> getBullets() {
        return Stream.of(bullet1, bullet2, bullet3, bullet4, bullet5)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void setCostUnit(String costUnit) {
        this.costUnit = costUnit;
    }

    public void setCostUSD(String costUSD) {
        this.costUSD = costUSD;
    }

    public void setDefaultRetention(String defaultRetention) {
        this.defaultRetention = defaultRetention;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuotaLimit(String quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public void setQuotaWarn(String quotaWarn) {
        this.quotaWarn = quotaWarn;
    }

    public String getRepositoryPlan() {
        return repositoryPlan;
    }

    public void setRepositoryPlan(String repositoryPlan) {
        this.repositoryPlan = repositoryPlan;
    }

    public void setAccessDuringOutage(Boolean accessDuringOutage) {
        this.accessDuringOutage = accessDuringOutage;
    }

    public List<CostProxy> getCosts() {
        Map<String, Object> costMap = new HashMap<>();
        costMap.put("usd", costUSD);
        return Collections.singletonList(new CostProxy(costMap, costUnit));
    }

    public Map<String, Object> getServiceSettings() {
        Map<String, Object> serviceSettings = new HashMap<>();
        if (accessDuringOutage != null)
            serviceSettings.put("access-during-outage", accessDuringOutage);

        if (defaultRetention != null)
            serviceSettings.put("default-retention", defaultRetention);

        if (quotaLimit != null || quotaWarn != null) {
            Map quota = new HashMap<String, Object>();
            if (quotaLimit != null)
                quota.put("limit", quotaLimit);

            if (quotaWarn != null)
                quota.put("warn",  quotaWarn);
            serviceSettings.put("quota", quota);
        }
        return serviceSettings;
    }
}
