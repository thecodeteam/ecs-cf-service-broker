package com.emc.ecs.management.sdk.model.iam.policy.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class IAMPolicyStatement {

    private String sid;
    private String effect;
    private String principal;
    private List<String> action;
    private List<String> resource;

    public IAMPolicyStatement sid(String sid) {
        this.sid = sid;
        return this;
    }

    public IAMPolicyStatement effect(String effect) {
        this.effect = effect;
        return this;
    }

    public IAMPolicyStatement principal(String principal) {
        this.principal = principal;
        return this;
    }

    public IAMPolicyStatement action(String action) {
        if (this.action == null) {
            this.action = Collections.singletonList(action);
        } else {
            this.action.add(action);
        }
        return this;
    }

    public IAMPolicyStatement action(List<String> action) {
        this.action = action;
        return this;
    }

    public IAMPolicyStatement resource(String resource) {
        if (this.resource == null) {
            this.resource = Collections.singletonList(resource);
        } else {
            this.resource.add(resource);
        }
        return this;
    }

    public IAMPolicyStatement resource(List<String> resource) {
        this.resource = resource;
        return this;
    }

    @JsonProperty("Sid")
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @JsonProperty("Effect")
    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    @JsonProperty("Action")
    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    @JsonProperty("Resource")
    public List<String> getResource() {
        return resource;
    }

    public void setResource(List<String> resource) {
        this.resource = resource;
    }

    @JsonProperty("Principal")
    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

}
