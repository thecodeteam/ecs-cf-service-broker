package com.emc.ecs.management.sdk.model.iam.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.*;

/**
 * IamPolicy
 */

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
public class IamPolicy {
    
    @JsonProperty(value="Arn")
    @XmlElement(name = "Arn")
    private String arn;

    @JsonProperty(value="AttachmentCount")
    @XmlElement(name = "AttachmentCount")
    private Integer attachmentCount;

    @JsonProperty(value="CreateDate")
    @XmlElement(name = "CreateDate")
    private String createDate;

    @JsonProperty(value="DefaultVersionId")
    @XmlElement(name = "DefaultVersionId")
    private String defaultVersionId;

    @JsonProperty(value="Description")
    @XmlElement(name = "Description")
    private String description;

    @JsonProperty(value="IsAttachable")
    @XmlElement(name = "IsAttachable")
    private Boolean isAttachable;

    @JsonProperty(value="Path")
    @XmlElement(name = "Path")
    private String path;
    
    @JsonProperty(value="PermissionsBoundaryUsageCount")
    @XmlElement(name = "PermissionsBoundaryUsageCount")
    private Integer permissionsBoundaryUsageCount;

    @JsonProperty(value="PolicyId")
    @XmlElement(name = "PolicyId")
    private String policyId;

    @JsonProperty(value="PolicyName")
    @XmlElement(name = "PolicyName")
    private String policyName;

    @JsonProperty(value="UpdateDate")
    @XmlElement(name = "UpdateDate")
    private String updateDate;

    /**
      * The resource name of the policy.
      * @return arn
      */
  
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public IamPolicy arn(String arn) {
        this.arn = arn;
        return this;
    }

    /**
     * The number of entities (users, groups, and roles) that the policy is attached to.
     * @return attachmentCount
     */

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public IamPolicy attachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
        return this;
    }

    /**
      * The date and time, in ISO 8601 date-time format, when the policy was created.
      * @return createDate
      */
  
    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public IamPolicy createDate(String createDate) {
        this.createDate = createDate;
        return this;
    }

    /**
      * The identifier for the version of the policy that is set as the default version.
      * @return defaultVersionId
      */
  
    public String getDefaultVersionId() {
        return defaultVersionId;
    }

    public void setDefaultVersionId(String defaultVersionId) {
        this.defaultVersionId = defaultVersionId;
    }

    public IamPolicy defaultVersionId(String defaultVersionId) {
        this.defaultVersionId = defaultVersionId;
        return this;
    }

    /**
     * A friendly description of the policy.
     * @return description.
     */

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IamPolicy description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Specifies whether the policy can be attached to user, group, or role.
     * @return isAttachable.
     */

    public Boolean getIsAttachable() {
        return isAttachable;
    }

    public void setIsAttachable(Boolean isAttachable) {
        this.isAttachable = isAttachable;
    }

    public IamPolicy isAttachable(Boolean isAttachable) {
        this.isAttachable = isAttachable;
        return this;
    }

    /**
      * The path to the policy
      * @return path
      */
  
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IamPolicy path(String path) {
        this.path = path;
        return this;
    }

    /**
      * Resource name of the policy that is used to set permissions boundary for the policy.
      * @return permissionsBoundaryUsageCount
      */
  
    public Integer getPermissionsBoundaryUsageCount() {
        return permissionsBoundaryUsageCount;
    }

    public void setPermissionsBoundaryUsageCount(Integer permissionsBoundaryUsageCount) {
        this.permissionsBoundaryUsageCount = permissionsBoundaryUsageCount;
    }


    public IamPolicy permissionsBoundaryUsageCount(Integer permissionsBoundaryUsageCount) {
        this.permissionsBoundaryUsageCount = permissionsBoundaryUsageCount;
        return this;
    }


    /**
     * The stable and unique string identifying the policy.
     * @return policyId
     */

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public IamPolicy policyId(String policyId) {
        this.policyId = policyId;
        return this;
    }

    /**
     * The friendly name of the policy.
     * @return policyName
     */

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public IamPolicy policyName(String policyName) {
        this.policyName = policyName;
        return this;
    }

    /**
     * The date and time, in ISO 8601 date-time format, when the policy was created.
     * @return updateDate
     */

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public IamPolicy updateDate(String updateDate) {
        this.updateDate = updateDate;
        return this;
    }

}

