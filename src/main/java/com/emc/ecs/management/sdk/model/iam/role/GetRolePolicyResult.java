package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.common.InlinePolicyWithDocument;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(value = XmlAccessType.NONE)
public class GetRolePolicyResult extends InlinePolicyWithDocument {

    private String roleName;
    /**
     * Simple name identifying the role.
     */
    @XmlElement(name = "RoleName")
    @JsonProperty(value = "RoleName")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
