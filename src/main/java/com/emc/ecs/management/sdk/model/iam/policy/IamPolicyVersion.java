package com.emc.ecs.management.sdk.model.iam.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * IamUser
 */

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
public class IamPolicyVersion {

    @JsonProperty(value="CreateDate")
    @XmlElement(name = "CreateDate")
    private String createDate;

    @JsonProperty(value="Document")
    @XmlElement(name = "Document")
    private String document;

    @JsonProperty(value="IsDefaultVersion")
    @XmlElement(name = "IsDefaultVersion")
    private Boolean isDefaultVersion;

    @JsonProperty(value="VersionId")
    @XmlElement(name = "VersionId")
    private String versionId;

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

    public IamPolicyVersion createDate(String createDate) {
        this.createDate = createDate;
        return this;
    }


    /**
     * The policy document, URL-encoded compliant with RFC 3986.
     * @return document
     */

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public IamPolicyVersion document(String document) {
        this.document = document;
        return this;
    }

    /**
      * Specifies whether the policy version is set as the policy's default version.
      * @return isDefaultVersion
      */
  
    public Boolean getIsDefaultVersion() {
        return isDefaultVersion;
    }

    public void setIsDefaultVersion(Boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public IamPolicyVersion isDefaultVersion(Boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
        return this;
    }

    /**
     * The identifier for the policy version.
     * @return versionId
     */

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public IamPolicyVersion versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

}

