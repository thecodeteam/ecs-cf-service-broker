package com.emc.ecs.serviceBroker.model;

import java.util.HashMap;
import java.util.Map;

public class CostProxy {

	private Map<String, Object> amount;
	private String unit;

	public CostProxy() {
		super();
	}

	public CostProxy(Map<String, Object> amount, String unit) {
		super();
		this.amount = amount;
		this.unit = unit;
	}

	public Map<String, Object> getAmount() {
		return amount;
	}

	public void setAmount(Map<String, Object> amount) {
		this.amount = amount;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Map<String, Object> unproxy() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("amount", amount);
		map.put("unit", unit);
		return map;
	}
}