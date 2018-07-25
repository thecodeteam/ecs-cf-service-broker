package com.emc.ecs.cloudfoundry.broker.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class TileSelector {
    @JsonProperty("value")
    private String value;

    @JsonProperty("selected_option")
    private Map<String, Object> option;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, Object> getOption() {
        return option;
    }

    public void setOption(Map<String, Object> option) {
        this.option = option;
    }
}

