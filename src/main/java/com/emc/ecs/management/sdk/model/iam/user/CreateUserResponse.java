package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.*;

/**
 * CreateUserResponse
 */

@XmlRootElement(name = "CreateUserResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateUserResponse extends BaseIamResponse {

    public CreateUserResponse() {
    }

    public CreateUserResponse(IamUser createdUser) {
        this.getCreateUserResult().setUser(createdUser);
    }
    
    @JsonProperty(value="CreateUserResult")
    @XmlElement(name = "CreateUserResult")
    private CreateUserResult createUserResult = null;

    /**
     * Wrapper with details about the new IAM user.
     */
    public CreateUserResult getCreateUserResult() {
        if (createUserResult == null) {
            createUserResult = new CreateUserResult();
        }
        return createUserResult;
    }

    public CreateUserResponse withCreateUserResult(CreateUserResult createUserResult) {
        setCreateUserResult(createUserResult);
        return this;
    }

    public void setCreateUserResult(CreateUserResult createUserResult) {
        this.createUserResult = createUserResult;
    }
}

