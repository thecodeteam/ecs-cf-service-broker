package com.emc.ecs.serviceBroker.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class PlanMetadataProxy {

	private List<String> bullets;
	private List<CostProxy> costs;
	private int storageLimit;

	public PlanMetadataProxy() {
		super();
	}

	public PlanMetadataProxy(List<String> bullets, List<CostProxy> costs,
			int storageLimit) {
		super();
		this.bullets = bullets;
		this.costs = costs;
		this.storageLimit = storageLimit;
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

	public Map<String, Object> unproxy() {
		Map<String, Object> map = new HashMap<>();
		map.put("bullets", bullets);
		if (costs != null)
			map.put("costs", costs.stream()
					.map(c -> c.unproxy())
					.collect(Collectors.toList()));
		return map;
	}

	public int getStorageLimit() {
		return storageLimit;
	}

	public void setStorageLimit(int storageLimit) {
		this.storageLimit = storageLimit;
	}

}