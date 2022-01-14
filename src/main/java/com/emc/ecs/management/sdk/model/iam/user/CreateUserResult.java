package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.*;

/**
 * CreateUserResult
 */

@XmlRootElement(name = "CreateUserResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateUserResult  {
    
    @JsonProperty(value="User")
    @XmlElement(name = "User")
    private IamUser user = null;

    public CreateUserResult user(IamUser user) {
        this.user = user;
        return this;
    }

    /**
     * Contains IAM User information.
     */
    public IamUser getUser() {
        return user;
    }

    public void setUser(IamUser user) {
        this.user = user;
    }

}

