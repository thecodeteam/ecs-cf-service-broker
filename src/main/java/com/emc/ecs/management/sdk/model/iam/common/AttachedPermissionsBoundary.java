package com.emc.ecs.management.sdk.model.iam.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PermissionsBoundary")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName(value = "PermissionsBoundary")
public class AttachedPermissionsBoundary {
    public static final String PERMISSIONS_BOUNDARY_TYPE = "Policy";
    private String permissionsBoundaryArn;
    private String permissionsBoundaryType = PERMISSIONS_BOUNDARY_TYPE;

    /**
     * The ARN of the policy set as permissions boundary.
     */
    @XmlElement(name = "PermissionsBoundaryArn")
    @JsonProperty(value = "PermissionsBoundaryArn")
    public String getPermissionsBoundaryArn() {
        return permissionsBoundaryArn;
    }

    public void setPermissionsBoundaryArn(String permissionsBoundaryArn) {
        this.permissionsBoundaryArn = permissionsBoundaryArn;
    }

    public AttachedPermissionsBoundary withArn(String permissionsBoundaryArn) {
        this.permissionsBoundaryArn = permissionsBoundaryArn;
        return this;
    }

    /**
     * The permissions boundary usage type that indicates what type of IAM resource is used as the
     * permissions boundary for an entity. This data type can only have a value of Policy.
     */
    @XmlElement(name = "PermissionsBoundaryType")
    @JsonProperty(value = "PermissionsBoundaryType")
    public String getPermissionsBoundaryType() {
        return permissionsBoundaryType;
    }

    public void setPermissionsBoundaryType(String permissionsBoundaryType) {
        this.permissionsBoundaryType = permissionsBoundaryType;
    }

    public AttachedPermissionsBoundary withType(String permissionsBoundaryType) {
        this.permissionsBoundaryType = permissionsBoundaryType;
        return this;
    }
}
