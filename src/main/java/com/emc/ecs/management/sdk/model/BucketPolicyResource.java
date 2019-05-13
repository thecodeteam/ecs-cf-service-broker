package com.emc.ecs.management.sdk.model;

import java.util.Arrays;
import java.util.List;

public class BucketPolicyResource {

    private List<String> resources;

    public BucketPolicyResource() {
        super();
    }

    public BucketPolicyResource(List<String> resources) {
        super();
        this.resources = resources;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }
}
