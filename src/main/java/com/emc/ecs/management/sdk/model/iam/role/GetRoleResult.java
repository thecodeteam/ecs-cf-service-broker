package com.emc.ecs.management.sdk.model.iam.role;

import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GetRoleResult")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("GetRoleResult")
public class GetRoleResult {

    private IamRole role;

    @XmlElementRef
    public IamRole getRole() {
        return role;
    }

    public void setRole(IamRole role) {
        this.role = role;
    }
}
