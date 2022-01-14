package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.common.AttachedPolicy;
import com.emc.ecs.management.sdk.model.iam.common.AttachedPolicyList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ListAttachedUserPoliciesResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListAttachedUserPoliciesResponse")
public class ListAttachedUserPoliciesResponse extends BaseIamResponse {

    private AttachedPolicyList result;

    public ListAttachedUserPoliciesResponse() {
    }

    public ListAttachedUserPoliciesResponse(List<AttachedPolicy> attachedPolicies, final String nextMarker) {
        this.getResult().setAttachedPolicies(attachedPolicies);
        this.getResult().setMarker(nextMarker);
    }

    @XmlElement(name = "ListAttachedUserPoliciesResult")
    @JsonProperty(value = "ListAttachedUserPoliciesResult")
    public AttachedPolicyList getResult() {
        if (result == null) {
            result = new AttachedPolicyList();
        }
        return result;
    }

    public void setResult(AttachedPolicyList result) {
        this.result = result;
    }
}
