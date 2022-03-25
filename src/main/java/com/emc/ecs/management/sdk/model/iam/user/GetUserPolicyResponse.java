package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GetUserPolicyResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("GetUserPolicyResponse")
public class GetUserPolicyResponse extends BaseIamResponse {

    private GetUserPolicyResult result;

    public GetUserPolicyResponse() {
    }

    public GetUserPolicyResponse(String roleName, String policyName, String policyDocument) {
        getResult().setUserName(roleName);
        getResult().setPolicyName(policyName);
        getResult().setPolicyDocument(policyDocument);
    }

    @XmlElement(name = "GetUserPolicyResult")
    @JsonProperty("GetUserPolicyResult")
    public GetUserPolicyResult getResult() {
        if (result == null) {
            result = new GetUserPolicyResult();
        }
        return result;
    }

    public void setResult(GetUserPolicyResult result) {
        this.result = result;
    }
}
