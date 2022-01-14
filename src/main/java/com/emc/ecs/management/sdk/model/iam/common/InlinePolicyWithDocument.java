package com.emc.ecs.management.sdk.model.iam.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class InlinePolicyWithDocument {

    private String policyName;
    private String policyDocument;

    /**
     * Simple name identifying the policy.
     */
    @XmlElement(name = "PolicyName")
    @JsonProperty(value = "PolicyName")
    public String getPolicyName() {
        return policyName;
    }

    /**
     * The policy Document.
     */
    @XmlElement(name = "PolicyDocument")
    @JsonProperty(value = "PolicyDocument")
    public String getPolicyDocument() {
        return policyDocument;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public void setPolicyDocument(String policyDocument) {
        this.policyDocument = policyDocument;
    }
}
