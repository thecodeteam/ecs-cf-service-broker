package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "GetRoleResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("GetRoleResponse")
public class GetRoleResponse extends BaseIamResponse {
    private GetRoleResult result;

    public GetRoleResponse() {
    }

    public GetRoleResponse(IamRole role) {
        this.getResult().setRole(role);
    }

    @XmlElementRef
    public GetRoleResult getResult() {
        if (result == null) {
            result = new GetRoleResult();
        }
        return result;
    }

    public void setResult(GetRoleResult result) {
        this.result = result;
    }
}
