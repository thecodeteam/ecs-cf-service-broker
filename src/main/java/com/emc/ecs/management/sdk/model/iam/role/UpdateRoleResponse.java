package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "UpdateRoleResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("UpdateRoleResponse")
public class UpdateRoleResponse extends BaseIamResponse {

    private UpdateRoleResult result;

    public UpdateRoleResponse() {
    }

    public UpdateRoleResponse(IamRole createdRole) {
        this.getResult().setRole(createdRole);
    }

    @XmlElementRef
    public UpdateRoleResult getResult() {
        if (result == null) {
            result = new UpdateRoleResult();
        }
        return result;
    }

    public void setResult(UpdateRoleResult result) {
        this.result = result;
    }
}
