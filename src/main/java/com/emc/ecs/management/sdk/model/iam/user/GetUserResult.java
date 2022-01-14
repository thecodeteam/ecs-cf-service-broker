package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.*;

/**
 * GetUserResult
 */

@XmlRootElement(name = "GetUserResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetUserResult  {
    
    @JsonProperty(value="User")
    @XmlElement(name = "User")
    private IamUser user = null;

    public GetUserResult user(IamUser user) {
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

