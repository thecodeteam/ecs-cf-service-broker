package com.emc.ecs.management.sdk.model.iam.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * CreatePolicyResult
 */

@XmlRootElement(name = "CreatePolicyResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreatePolicyVersionResult {
    
    @JsonProperty(value="PolicyVersion")
    @XmlElement(name = "PolicyVersion")
    private IamPolicyVersion policyVersion = null;

    public CreatePolicyVersionResult policyVersion(IamPolicyVersion policyVersion) {
        this.policyVersion = policyVersion;
        return this;
    }

    /**
      * Get policyVersion
      * @return policyVersion
      */

    public IamPolicyVersion getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(IamPolicyVersion policyVersion) {
        this.policyVersion = policyVersion;
    }

}

