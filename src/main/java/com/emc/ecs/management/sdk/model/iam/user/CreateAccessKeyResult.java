package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * CreateAccessKeyResult
 */

@XmlRootElement(name = "CreateAccessKeyResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateAccessKeyResult {

    @JsonProperty(value="AccessKey")
    @XmlElement(name = "AccessKey")
    private IamAccessKey accessKey = null;

    public CreateAccessKeyResult accessKey(IamAccessKey accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public IamAccessKey getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(IamAccessKey accessKey) {
        this.accessKey = accessKey;
    }

    public CreateAccessKeyResult withAccessKey(IamAccessKey accessKey) {
        this.accessKey = accessKey;
        return this;
    }
}

