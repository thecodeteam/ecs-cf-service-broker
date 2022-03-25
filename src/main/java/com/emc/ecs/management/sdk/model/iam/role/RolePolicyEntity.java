package com.emc.ecs.management.sdk.model.iam.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "member")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@XmlType(propOrder = {"roleName", "roleId"})
@JsonRootName(value = "member")
public class RolePolicyEntity {
    private String roleName;
    private String roleId;
    /**
     * Simple name identifying the role.
     */
    @XmlElement(name = "RoleName")
    @JsonProperty(value = "RoleName")
    public String getRoleName() {
        return roleName;
    }
    /**
     * Unique Id associated with the role.
     */
    @XmlElement(name = "RoleId")
    @JsonProperty(value = "RoleId")
    public String getRoleId() {
        return roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public RolePolicyEntity() {}
    public RolePolicyEntity(String roleName, String roleId) {
        this.roleName = roleName;
        this.roleId = roleId;
    }
}

