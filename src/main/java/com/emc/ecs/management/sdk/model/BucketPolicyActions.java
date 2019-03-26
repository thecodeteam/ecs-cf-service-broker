package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class BucketPolicyActions {

    private List<String> actions;

    public BucketPolicyActions() {
        super();
    }

    public BucketPolicyActions(List<String> actions) {
        super();
        this.actions = actions;
    }

    @JsonProperty
    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }
}
