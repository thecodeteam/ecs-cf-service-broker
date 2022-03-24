package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * UpdateUserResult
 */

@XmlRootElement(name = "UpdateUserResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateUserResult {

    @JsonProperty(value="User")
    @XmlElement(name = "User")
    private IamUser user = null;

    public UpdateUserResult user(IamUser user) {
        this.user = user;
        return this;
    }

    public IamUser getUser() {
        return user;
    }

    public void setUser(IamUser user) {
        this.user = user;
    }
}

