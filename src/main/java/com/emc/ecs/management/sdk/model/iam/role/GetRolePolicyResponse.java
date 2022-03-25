package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GetRolePolicyResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("GetRolePolicyResponse")
public class GetRolePolicyResponse extends BaseIamResponse {

    private GetRolePolicyResult result;

    public GetRolePolicyResponse() {
    }

    public GetRolePolicyResponse(String roleName, String policyName, String policyDocument) {
        getResult().setRoleName(roleName);
        getResult().setPolicyName(policyName);
        getResult().setPolicyDocument(policyDocument);
    }

    @XmlElement(name = "GetRolePolicyResult")
    @JsonProperty("GetRolePolicyResult")
    public GetRolePolicyResult getResult() {
        if (result == null) {
            result = new GetRolePolicyResult();
        }
        return result;
    }

    public void setResult(GetRolePolicyResult result) {
        this.result = result;
    }
}
