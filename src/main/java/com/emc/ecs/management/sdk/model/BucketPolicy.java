package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class BucketPolicy {

    private String version;
    private String id;
    private List<BucketPolicyStatement> bucketPolicyStatements = new ArrayList<>();

    public BucketPolicy() {
        super();
    }

    public BucketPolicy(String version, String id, List<BucketPolicyStatement> bucketPolicyStatements) {
        super();
        this.version = version;
        this.id = id;
        this.bucketPolicyStatements = bucketPolicyStatements;
    }

    @JsonProperty("Version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("Statement")
    public List<BucketPolicyStatement> getBucketPolicyStatements() {
        return bucketPolicyStatements;
    }

    public void setBucketPolicyStatements(List<BucketPolicyStatement> bucketPolicyStatements) {
        this.bucketPolicyStatements = bucketPolicyStatements;
    }
}
