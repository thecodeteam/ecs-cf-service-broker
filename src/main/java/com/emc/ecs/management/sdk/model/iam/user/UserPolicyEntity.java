package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "member")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@XmlType(propOrder = {"userName", "userId"})
@JsonRootName(value = "member")
public class UserPolicyEntity {
    private String userName;
    private String userId;

    public UserPolicyEntity() {}
    public UserPolicyEntity(String name, String id) {
        this.userName = name;
        this.userId = id;
    }

    /**
     * Simple name identifying the user.
     */
    @XmlElement(name = "UserName")
    @JsonProperty(value = "UserName")
    public String getUserName() {
        return userName;
    }

    /**
     * Unique Id associated with the user.
     */
    @XmlElement(name = "UserId")
    @JsonProperty(value = "UserId")
    public String getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
