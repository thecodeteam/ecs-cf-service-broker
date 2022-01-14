package com.emc.ecs.management.sdk.model.iam.policy;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.ResponseMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * CreatePolicyVersionResponse
 */

@XmlRootElement(name = "CreatePolicyVersionResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreatePolicyVersionResponse extends BaseIamResponse {
    
    @JsonProperty(value="CreatePolicyVersionResult")
    @XmlElement(name = "CreatePolicyVersionResult")
    private CreatePolicyVersionResult createPolicyVersionResult = null;

    public CreatePolicyVersionResponse createPoicyVersionResult(CreatePolicyVersionResult result) {
        this.createPolicyVersionResult = result;
        return this;
    }

    /**
      * Get createPolicyVersionResult
      * @return createPolicyVersionResult
      */

    public CreatePolicyVersionResult getCreatePolicyVersionResult() {
        return createPolicyVersionResult;
    }

    public void setCreatePolicyVersionResult(CreatePolicyVersionResult result) {
        this.createPolicyVersionResult = result;
    }

    public CreatePolicyVersionResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

