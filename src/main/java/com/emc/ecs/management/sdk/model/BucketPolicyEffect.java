package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BucketPolicyEffect {

    private String effect;

    public BucketPolicyEffect() {
        super();
    }

    public BucketPolicyEffect(String effect) {
        super();
        this.effect = effect;
    }

    @JsonProperty
    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}
