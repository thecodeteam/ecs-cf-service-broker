package com.emc.ecs.management.sdk.model.iam.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "AttachedPolicy")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@XmlType(propOrder = {"policyName", "policyArn"})
@JsonRootName(value = "AttachedPolicy")
public class AttachedPolicy {
    private String policyName;
    private String policyArn;

    /**
     * The simple name of the policy.
     */
    @XmlElement(name = "PolicyName")
    @JsonProperty(value = "PolicyName")
    public String getPolicyName() {
        return policyName;
    }
    /**
     * The ARN of the attached policy.
     */
    @XmlElement(name = "PolicyArn")
    @JsonProperty(value = "PolicyArn")
    public String getPolicyArn() {
        return policyArn;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public void setPolicyArn(String policyArn) {
        this.policyArn = policyArn;
    }
}
