package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.common.InlinePolicyWithDocument;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GetUserPolicyResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetUserPolicyResult extends InlinePolicyWithDocument {

    @JsonProperty(value="UserName")
    @XmlElement(name = "UserName")
    private String userName = null;

    /**
     * Friendly name of the user.
     */
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

