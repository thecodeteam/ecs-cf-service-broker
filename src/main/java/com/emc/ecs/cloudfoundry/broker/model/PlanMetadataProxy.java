package com.emc.ecs.cloudfoundry.broker.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Component
public class PlanMetadataProxy {

    private List<String> bullets;
    private List<CostProxy> costs;

    public PlanMetadataProxy() {
        super();
    }

    public PlanMetadataProxy(List<String> bullets, List<CostProxy> costs) {
        super();
        this.bullets = bullets;
        this.costs = costs;
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