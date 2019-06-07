package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BucketPolicy {

    private String version;
    private String id;
    private BucketPolicyStatement bucketPolicyStatement;

    public BucketPolicy() {
        super();
    }

    public BucketPolicy(String version, String id, BucketPolicyStatement bucketPolicyStatement) {
        super();
        this.version = version;
        this.id = id;
        this.bucketPolicyStatement = bucketPolicyStatement;
    }

    @JsonProperty("Version")
    String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("Id")
    String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("Statement")
    BucketPolicyStatement getBucketPolicyStatement() {
        return bucketPolicyStatement;
    }

    public void setBucketPolicyStatement(BucketPolicyStatement bucketPolicyStatement) {
        this.bucketPolicyStatement = bucketPolicyStatement;
    }
}
