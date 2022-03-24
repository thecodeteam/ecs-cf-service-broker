package com.emc.ecs.management.sdk.model.iam.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * GetPolicyResult
 */

@XmlRootElement(name = "GetPolicyVersionResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetPolicyVersionResult {
    
    @JsonProperty(value="PolicyVersion")
    @XmlElement(name = "PolicyVersion")
    private IamPolicyVersion policyVersion = null;

    public GetPolicyVersionResult policyVersion(IamPolicyVersion policyVersion) {
        this.policyVersion = policyVersion;
        return this;
    }

    /**
      * Get policyVersion
      * @return policyVersion
      */

    public IamPolicyVersion getPolicyVersioin() {
        return policyVersion;
    }

    public void setPolicyVersioin(IamPolicyVersion policyVersioin) {
        this.policyVersion = policyVersioin;
    }


}

