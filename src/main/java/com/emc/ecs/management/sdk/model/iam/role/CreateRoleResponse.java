package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CreateRoleResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("CreateRoleResponse")
public class CreateRoleResponse extends BaseIamResponse {

    private CreateRoleResult result;

    public CreateRoleResponse() {
    }

    public CreateRoleResponse(IamRole createdRole) {
        this.getResult().setRole(createdRole);
    }

    @XmlElementRef
    public CreateRoleResult getResult() {
        if (result == null) {
            result = new CreateRoleResult();
        }
        return result;
    }

    public void setResult(CreateRoleResult result) {
        this.result = result;
    }
}
