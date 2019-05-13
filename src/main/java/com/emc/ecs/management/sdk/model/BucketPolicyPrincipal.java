package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BucketPolicyPrincipal {

    private String principal;

    public BucketPolicyPrincipal() {
        super();
    }

    public BucketPolicyPrincipal(String principal) {
        super();
        this.principal = principal;
    }

    @JsonProperty
    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
