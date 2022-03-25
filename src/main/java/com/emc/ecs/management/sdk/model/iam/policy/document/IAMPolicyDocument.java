package com.emc.ecs.management.sdk.model.iam.policy.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IAMPolicyDocument {
    private String version;
    private String id;
    private List<IAMPolicyStatement> statements;

    public IAMPolicyDocument(String version, String id, List<IAMPolicyStatement> statements) {
        this.version = version;
        this.id = id;
        this.statements = statements;
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
    public List<IAMPolicyStatement> getStatements() {
        return statements;
    }

    public void setStatements(List<IAMPolicyStatement> statements) {
        this.statements = statements;
    }
}
