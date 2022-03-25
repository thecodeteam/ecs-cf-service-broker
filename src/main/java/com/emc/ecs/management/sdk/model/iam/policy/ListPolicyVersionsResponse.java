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

@XmlRootElement(name = "ListPolicyVersionsResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ListPolicyVersionsResponse extends BaseIamResponse {
    
    @JsonProperty(value="ListPolicyVersionsResult")
    @XmlElement(name = "ListPolicyVersionsResult")
    private ListPolicyVersionsResult listPolicyVersionsResult = null;

    public ListPolicyVersionsResponse listPolicyVersionsResult(ListPolicyVersionsResult result) {
        this.listPolicyVersionsResult = result;
        return this;
    }

    /**
      * Get listPolicyVersionsResult
      * @return listPolicyVersionsResult
      */

    public ListPolicyVersionsResult getListPolicyVersionsResult() {
        return listPolicyVersionsResult;
    }

    public void setListPolicyVersionsResult(ListPolicyVersionsResult result) {
        this.listPolicyVersionsResult = result;
    }

    public ListPolicyVersionsResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

