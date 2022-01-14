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
 * ListPoliciesResponse
 */

@XmlRootElement(name = "ListPoliciesResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ListPoliciesResponse extends BaseIamResponse {
    
    @JsonProperty(value="ListPoliciesResult")
    @XmlElement(name = "ListPoliciesResult")
    private ListPoliciesResult listPoliciesResult = null;
    
    /**
     * Get listPoliciesResult
     * @return listPoliciesResult
     */

    public ListPoliciesResult getListPoliciesResult() {
        return listPoliciesResult;
    }

    public void setListPoliciesResult(ListPoliciesResult listPoliciesResult) {
        this.listPoliciesResult = listPoliciesResult;
    }

    public ListPoliciesResponse listPoliciesResult(ListPoliciesResult listPoliciesResult) {
        this.listPoliciesResult = listPoliciesResult;
        return this;
    }

    public ListPoliciesResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

