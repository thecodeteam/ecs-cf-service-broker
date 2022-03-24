package com.emc.ecs.management.sdk.model.iam.role;

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

@XmlRootElement(name = "ListAttachedRolePoliciesResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListAttachedRolePoliciesResponse")
public class ListAttachedRolePoliciesResponse extends BaseIamResponse {

    private AttachedPolicyList result;

    public ListAttachedRolePoliciesResponse() {
    }

    public ListAttachedRolePoliciesResponse(List<AttachedPolicy> attachedPolicies, final String nextMarker) {
        this.getResult().setAttachedPolicies(attachedPolicies);
        this.getResult().setMarker(nextMarker);
    }

    @XmlElement(name = "ListAttachedRolePoliciesResult")
    @JsonProperty(value = "ListAttachedRolePoliciesResult")
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
