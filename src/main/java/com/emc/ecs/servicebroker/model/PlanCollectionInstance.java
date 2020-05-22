package com.emc.ecs.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "WeakerAccess"})
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

    @JsonProperty("cost_value")
    private String costValue;

    @JsonProperty("cost_currency")
    private String costCurrency;

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
    private Boolean repositoryPlan;

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

    public void setCostValue(String costValue) {
        this.costValue = costValue;
    }

    public void setCostCurrency(String costCurrency) {
        this.costCurrency = costCurrency;
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

    public Boolean getRepositoryPlan() {
        return repositoryPlan;
    }

    public void setRepositoryPlan(Boolean repositoryPlan) {
        this.repositoryPlan = repositoryPlan;
    }

    public void setAccessDuringOutage(Boolean accessDuringOutage) {
        this.accessDuringOutage = accessDuringOutage;
    }

    public List<CostProxy> getCosts() {
        Map<String, Double> val = new HashMap<>();
        val.put(costCurrency != null ? costCurrency.toLowerCase() : Currency.getInstance("USD").getCurrencyCode().toLowerCase(), Double.valueOf(costValue));

        CostProxy costProxy = new CostProxy();
        costProxy.setUnit(costUnit);
        costProxy.setAmount(val);

        return Collections.singletonList(costProxy);
    }

    public Map<String, Object> getServiceSettings() {
        Map<String, Object> serviceSettings = new HashMap<>();
        if (accessDuringOutage != null)
            serviceSettings.put("access-during-outage", accessDuringOutage);

        if (defaultRetention != null)
            serviceSettings.put("default-retention", Integer.parseInt(defaultRetention));

        if (quotaLimit != null || quotaWarn != null) {
            Map<String, Integer> quota = new HashMap<>();
            if (quotaLimit != null)
                quota.put("limit", Integer.parseInt(quotaLimit));

            if (quotaWarn != null)
                quota.put("warn",  Integer.parseInt(quotaWarn));
            serviceSettings.put("quota", quota);
        }
        return serviceSettings;
    }
}
