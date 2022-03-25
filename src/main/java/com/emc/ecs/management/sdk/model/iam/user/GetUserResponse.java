package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.*;

/**
 * GetUserResponse
 */

@XmlRootElement(name = "GetUserResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class GetUserResponse extends BaseIamResponse {

    public GetUserResponse() {
    }
    public GetUserResponse(IamUser user) {
        this.geUserResult().setUser(user);
    }

    @JsonProperty(value="GetUserResult")
    @XmlElement(name = "GetUserResult")
    private GetUserResult getUserResult = null;

    public GetUserResult geUserResult() {
        if (getUserResult == null) {
            getUserResult = new GetUserResult();
        }
        return getUserResult;
    }

    public void setGetUserResult(GetUserResult getUserResult) {
        this.getUserResult = getUserResult;
    }
}

