package com.emc.ecs.servicebroker.model;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class CostProxy {

    private Map<String, Double> amount;
    private String unit;

    public CostProxy() {
        super();
    }

    public CostProxy(Map<String, Double> amount, String unit) {
        super();
        this.amount = amount;
        this.unit = unit;
    }

    public Map<String, Double> getAmount() {
        return amount;
    }

    public void setAmount(Map<String, Double> amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    Map<String, Object> unproxy() {
        Map<String, Object> map = new HashMap<>();
        map.put("amount", amount);
        map.put("unit", unit);
        return map;
    }
}