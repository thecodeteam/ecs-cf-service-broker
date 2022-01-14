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
 * GetPolicyVersionResponse
 */

@XmlRootElement(name = "GetPolicyVersionResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class GetPolicyVersionResponse extends BaseIamResponse {
    
    @JsonProperty(value="GetPolicyVersionResult")
    @XmlElement(name = "GetPolicyVersionResult")
    private GetPolicyVersionResult getPolicyVersionResult = null;

    public GetPolicyVersionResponse getPolicyVersionResponse(GetPolicyVersionResult result) {
        this.getPolicyVersionResult = result;
        return this;
    }

    /**
      * Get getPolicyVersionResult
      * @return getPolicyVersionResult
      */

    public GetPolicyVersionResult getGetPolicyVersionResult() {
        return getPolicyVersionResult;
    }

    public void setGetPolicyVersionResult(GetPolicyVersionResult result) {
        this.getPolicyVersionResult = result;
    }

    public GetPolicyVersionResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

