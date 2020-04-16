package com.emc.ecs.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TileSelector {
    private String value;

    @JsonProperty("selected_option")
    private Map<String, Object> selectedOption;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, Object> getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Map<String, Object> option) {
        this.selectedOption = option;
    }
}

