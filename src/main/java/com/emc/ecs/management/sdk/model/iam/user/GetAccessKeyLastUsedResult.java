package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "GetAccessKeyLastUsedResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetAccessKeyLastUsedResult {

    @JsonProperty(value="AccessKeyLastUsed")
    @XmlElement(name = "AccessKeyLastUsed")
    private IamAccessKeyLastUsed accessKeyLastUsed;

    @JsonProperty(value="UserName")
    @XmlElement(name = "UserName")
    private String userName;

    public IamAccessKeyLastUsed getAccessKeyLastUsed() {
        return accessKeyLastUsed;
    }

    public void setAccessKeyLastUsed(IamAccessKeyLastUsed accessKeyLastUsed) {
        this.accessKeyLastUsed = accessKeyLastUsed;
    }

    public GetAccessKeyLastUsedResult withAccessKeyLastUsed(IamAccessKeyLastUsed accessKeyLastUsed) {
        this.accessKeyLastUsed = accessKeyLastUsed;
        return this;
    }

    /**
     * Name of the AWS IAM user that owns this access key.
     */
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public GetAccessKeyLastUsedResult withUserName(String userName) {
        this.userName = userName;
        return this;
    }
}

