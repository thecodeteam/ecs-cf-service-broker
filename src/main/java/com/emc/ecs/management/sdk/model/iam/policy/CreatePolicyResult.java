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
public class CreatePolicyResult {
    
    @JsonProperty(value="Policy")
    @XmlElement(name = "Policy")
    private IamPolicy policy = null;

    public CreatePolicyResult policy(IamPolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
      * Get policy
      * @return policy
      */

    public IamPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(IamPolicy policy) {
        this.policy = policy;
    }


}

