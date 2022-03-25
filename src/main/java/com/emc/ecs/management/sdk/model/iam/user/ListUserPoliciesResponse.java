package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.common.InlinePolicyList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ListUserPoliciesResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListUserPoliciesResponse")
public class ListUserPoliciesResponse extends BaseIamResponse {

    private InlinePolicyList result;

    public ListUserPoliciesResponse() {
    }

    public ListUserPoliciesResponse(List<String> inlinePolicyNames, String nextMarker) {
        getResult().setInlinePolicyNames(inlinePolicyNames);
        getResult().setMarker(nextMarker);
    }

    @XmlElement(name = "ListUserPoliciesResult")
    @JsonProperty("ListUserPoliciesResult")
    public InlinePolicyList getResult() {
        if (result == null) {
            result = new InlinePolicyList();
        }
        return result;
    }

    public void setResult(InlinePolicyList result) {
        this.result = result;
    }
}
