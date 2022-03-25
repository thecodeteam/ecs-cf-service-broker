package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.ResponseMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * UpdateUserResponse
 */

@XmlRootElement(name = "UpdateUserResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateUserResponse extends BaseIamResponse {

    @JsonProperty(value="UpdateUserResult")
    @XmlElement(name = "UpdateUserResult")
    private UpdateUserResult updateUserResult = null;

    public UpdateUserResult getUpdateUserResult() {
        return updateUserResult;
    }

    public void setUpdateUserResult(UpdateUserResult updateUserResult) {
        this.updateUserResult = updateUserResult;
    }

    public UpdateUserResponse updateUserResult(UpdateUserResult updateUserResult) {
        this.updateUserResult = updateUserResult;
        return this;
    }

    public UpdateUserResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }
}

