package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class BucketPolicyStatement {

    private String sid;
    private String bucketPolicyEffect;
    private String bucketPolicyPrincipal;
    private List<String> bucketPolicyAction;
    private List<String> bucketPolicyResource;

    public BucketPolicyStatement() {
        super();
    }

    public BucketPolicyStatement(String statementId,
                                 BucketPolicyEffect bucketPolicyEffect,
                                 BucketPolicyPrincipal bucketPolicyPrincipal,
                                 BucketPolicyActions bucketPolicyAction,
                                 BucketPolicyResource bucketPolicyResource) {
        super();
        this.sid = statementId;
        this.bucketPolicyEffect = bucketPolicyEffect.getEffect();
        this.bucketPolicyPrincipal = bucketPolicyPrincipal.getPrincipal();
        this.bucketPolicyAction = bucketPolicyAction.getActions();
        this.bucketPolicyResource = bucketPolicyResource.getResources();
    }

    @JsonProperty("Sid")
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @JsonProperty("Effect")
    public String getBucketPolicyEffect() {
        return bucketPolicyEffect;
    }

    public void setBucketPolicyEffect(String bucketPolicyEffect) {
        this.bucketPolicyEffect = bucketPolicyEffect;
    }

    @JsonProperty("Action")
    public List<String> getBucketPolicyAction() {
        return bucketPolicyAction;
    }

    public void setBucketPolicyAction(List<String> bucketPolicyAction) {
        this.bucketPolicyAction = bucketPolicyAction;
    }

    @JsonProperty("Resource")
    public List<String> getBucketPolicyResource() {
        return bucketPolicyResource;
    }

    public void setBucketPolicyResource(List<String> bucketPolicyResource) {
        this.bucketPolicyResource = bucketPolicyResource;
    }

    @JsonProperty("Principal")
    public String getPrincipal() {
        return bucketPolicyPrincipal;
    }

    public void setPrincipal(String principal) {
        this.bucketPolicyPrincipal = principal;
    }
}
