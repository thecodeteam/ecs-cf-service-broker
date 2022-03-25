package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;

/**
 * IAM user AccessKey
 */

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
public class IamAccessKey {

    public enum Status {
        Active,
        Inactive,
    }

    @JsonProperty(value="AccessKeyId")
    @XmlElement(name = "AccessKeyId")
    private String accessKeyId;

    @JsonProperty(value="AccessKeySelector")
    @XmlElement(name = "AccessKeySelector")
    private String accessKeySelector;

    @JsonProperty(value="CreateDate")
    @XmlElement(name = "CreateDate")
    private String createDate;

    @JsonProperty(value="SecretAccessKey")
    @XmlElement(name = "SecretAccessKey")
    private String secretAccessKey;

    @JsonProperty(value="Status")
    @XmlElement(name = "Status")
    private Status status;

    @JsonProperty(value="UserName")
    @XmlElement(name = "UserName")
    private String userName;

    /**
     * The Id of this access key
     * @return accessKeyId
     */

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public IamAccessKey accessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
        return this;
    }

    /**
     * The access key selector
     * @return accessKeySelector
     */
    public String getAccessKeySelector() { return accessKeySelector; }

    public void setAccessKeySelector(String accessKeySelector) { this.accessKeySelector = accessKeySelector; }

    public IamAccessKey withAccessKeySelector(String accessKeySelector) {
        this.accessKeySelector = accessKeySelector;
        return this;
    }

    /**
     * The date and time, in the format of YYYY-MM-DDTHH:mm:ssZ, when the access key was created.
     * @return createDate
     */

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public IamAccessKey withCreateDate(String createDate) {
        this.createDate = createDate;
        return this;
    }

    /**
     * The secret key
     * @return secretAccessKey
     */

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public IamAccessKey withSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
        return this;
    }

    /**
     *
     * The status of the access key {Active | Inactive}
     * @return status
     */

    public Status getStatus() { return status;}

    public void setStatus(String status) { this.status = Status.valueOf(status); }
    public void setStatus(Status status) { this.status = status; }

    public IamAccessKey withStatus(String status) {
        this.status = Status.valueOf(status);
        return this;
    }

    /**
     * The name of the user that the access key is associated with.
     * @return userName
     */

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public IamAccessKey withUserName(String userName) {
        this.userName = userName;
        return this;
    }

}

