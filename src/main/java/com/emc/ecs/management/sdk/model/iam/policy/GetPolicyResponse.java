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
 * GetPolicyResponse
 */

@XmlRootElement(name = "GetPolicyResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class GetPolicyResponse extends BaseIamResponse {
    
    @JsonProperty(value="GetPolicyResult")
    @XmlElement(name = "GetPolicyResult")
    private GetPolicyResult getPolicyResult = null;

    public GetPolicyResponse getPolicyResult(GetPolicyResult getPolicyResult) {
        this.getPolicyResult = getPolicyResult;
        return this;
    }

    /**
      * Get getPolicyResult
      * @return getPolicyResult
      */
    public GetPolicyResult getGetPolicyResult() {
        return getPolicyResult;
    }

    public void setGetPolicyResult(GetPolicyResult getPolicyResult) {
        this.getPolicyResult = getPolicyResult;
    }

    public GetPolicyResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

