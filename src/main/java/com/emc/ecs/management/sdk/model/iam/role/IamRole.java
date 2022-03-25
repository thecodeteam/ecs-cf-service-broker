package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.TagResponse;
import com.emc.ecs.management.sdk.model.iam.common.AttachedPermissionsBoundary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Role")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName(value = "Role")
public class IamRole {

    private String arn;
    private String assumeRolePolicyDocument;
    private String createDate;
    private String description;
    private Integer maxSessionDuration;
    private String path;
    private String roleId;
    private String roleName;
    private List<TagResponse> tags;
    private AttachedPermissionsBoundary permissionsBoundary;

    /**
     * Arn that identifies the role.
     */
    @XmlElement(name = "Arn")
    @JsonProperty(value = "Arn")
    public String getArn() {
        return arn;
    }
    /**
     * The trust relationship policy document that grants an entity permission to assume the role.
     */
    @XmlElement(name = "AssumeRolePolicyDocument")
    @JsonProperty(value = "AssumeRolePolicyDocument")
    public String getAssumeRolePolicyDocument() {
        return assumeRolePolicyDocument;
    }
    /**
     * ISO 8601 DateTime when role was created.
     */
    @XmlElement(name = "CreateDate")
    @JsonProperty(value = "CreateDate")
    public String getCreateDate() {
        return createDate;
    }
    /**
     * The description of the IAM role.
     */
    @XmlElement(name = "Description")
    @JsonProperty(value = "Description")
    public String getDescription() {
        return description;
    }
    /**
     * The maximum session duration (in seconds) that you want to set for the specified role.
     */
    @XmlElement(name = "MaxSessionDuration")
    @JsonProperty(value = "MaxSessionDuration")
    public Integer getMaxSessionDuration() {
        return maxSessionDuration;
    }
    /**
     * The path to the IAM role.
     */
    @XmlElement(name = "Path")
    @JsonProperty(value = "Path")
    public String getPath() {
        return path;
    }
    /**
     * Unique Id associated with the role.
     */
    @XmlElement(name = "RoleId")
    @JsonProperty(value = "RoleId")
    public String getRoleId() {
        return roleId;
    }
    /**
     * Simple name identifying the role.
     */
    @XmlElement(name = "RoleName")
    @JsonProperty(value = "RoleName")
    public String getRoleName() {
        return roleName;
    }
    /**
     * The list of Tags associated with the role.
     */
    @XmlElementWrapper(name = "Tags")
    @XmlElementRef
    @JsonProperty("Tags")
    public List<TagResponse> getTags() {
        return tags;
    }
    /**
     * The ARN of the policy used to set the permissions boundary for the role.
     */
    @JsonProperty(value="PermissionsBoundary")
    @XmlElement(name = "PermissionsBoundary")
    public AttachedPermissionsBoundary getPermissionsBoundary() {
        return permissionsBoundary;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public void setAssumeRolePolicyDocument(String assumeRolePolicyDocument) {
        this.assumeRolePolicyDocument = assumeRolePolicyDocument;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaxSessionDuration(Integer maxSessionDuration) {
        this.maxSessionDuration = maxSessionDuration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }

    public void setPermissionsBoundary(AttachedPermissionsBoundary permissionsBoundary) {
        this.permissionsBoundary = permissionsBoundary;
    }
}
