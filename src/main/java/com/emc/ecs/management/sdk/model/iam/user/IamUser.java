package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.TagResponse;
import com.emc.ecs.management.sdk.model.iam.common.AttachedPermissionsBoundary;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import javax.xml.bind.annotation.*;

/**
 * IamUser
 */

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
public class IamUser  {
    
    @JsonProperty(value="Arn")
    @XmlElement(name = "Arn")
    private String arn;

    @JsonProperty(value="CreateDate")
    @XmlElement(name = "CreateDate")
    private String createDate;

    @JsonProperty(value="PasswordLastUsed")
    @XmlElement(name = "PasswordLastUsed")
    private String passwordLastUsed;

    @JsonProperty(value="Path")
    @XmlElement(name = "Path")
    private String path;
    
    @JsonProperty(value="PermissionsBoundary")
    @XmlElement(name = "PermissionsBoundary")
    private AttachedPermissionsBoundary permissionsBoundary;
    
    @JsonProperty(value="Tags")
    @XmlElementWrapper(name = "Tags")
    @XmlElement(name = "member")
    private List<TagResponse> tags = null;

    @JsonProperty(value="UserId")
    @XmlElement(name = "UserId")
    private String userId;

    @JsonProperty(value="UserName")
    @XmlElement(name = "UserName")
    private String userName;

    public IamUser withArn(String arn) {
        this.arn = arn;
        return this;
    }

    /**
     * Arn that identifies the user.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public IamUser withCreateDate(String createDate) {
        this.createDate = createDate;
        return this;
    }

    /**
     * ISO 8601 format DateTime when user was created.
     */
    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public IamUser withPasswordLastUsed(String passwordLastUsed) {
        this.passwordLastUsed = passwordLastUsed;
        return this;
    }

    /**
     * ISO 8601 DateTime when the password was last used.
     */
    public String getPasswordLastUsed() {
        return passwordLastUsed;
    }

    public void setPasswordLastUsed(String passwordLastUsed) {
        this.passwordLastUsed = passwordLastUsed;
    }

    public IamUser withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * The path to the IAM User.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IamUser withPermissionsBoundary(AttachedPermissionsBoundary permissionsBoundary) {
        this.permissionsBoundary = permissionsBoundary;
        return this;
    }

    /**
     * The ARN of the policy used to set the permissions boundary for the user.
     */
    public AttachedPermissionsBoundary getPermissionsBoundary() {
        return permissionsBoundary;
    }

    public void setPermissionsBoundary(AttachedPermissionsBoundary permissionsBoundary) {
        this.permissionsBoundary = permissionsBoundary;
    }

    public IamUser withTags(List<TagResponse> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * The list of Tags associated with the User.
     */
    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }

    public IamUser withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Unique Id associated with the User.
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public IamUser withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Simple name identifying the User.
     */
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

