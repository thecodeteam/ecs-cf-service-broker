package com.emc.ecs.servicebroker.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Component
public class PlanMetadataProxy {

    private String displayName;
    private List<String> bullets;
    private List<CostProxy> costs;
    private Map<String, Object> properties;

    public PlanMetadataProxy() {
        super();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public PlanMetadataProxy(List<String> bullets, List<CostProxy> costs, String displayName, Map<String, Object> properties) {
        super();
        this.bullets = bullets;
        this.costs = costs;
        this.displayName = displayName;
        this.properties = properties;
    }

    public List<String> getBullets() {
        return bullets;
    }

    public void setBullets(List<String> bullets) {
        this.bullets = bullets;
    }

    public List<CostProxy> getCosts() {
        return costs;
    }

    public void setCosts(List<CostProxy> costs) {
        this.costs = costs;
    }

    Map<String, Object> unproxy() {
        Map<String, Object> map = new HashMap<>();
        map.put("bullets", bullets);
        if (costs != null)
            map.put("costs", costs.stream().map(CostProxy::unproxy)
                    .collect(Collectors.toList()));
        return map;
    }

}

