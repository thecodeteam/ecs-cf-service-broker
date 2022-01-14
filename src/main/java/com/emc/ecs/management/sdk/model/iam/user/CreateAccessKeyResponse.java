package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * CreateAccessKeyResponse
 */

@XmlRootElement(name = "CreateAccessKeyResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateAccessKeyResponse extends BaseIamResponse {

    @JsonProperty(value="CreateAccessKeyResult")
    @XmlElement(name = "CreateAccessKeyResult")
    private CreateAccessKeyResult createAccessKeyResult = null;

    public CreateAccessKeyResult getCreateAccessKeyResult() {
        return createAccessKeyResult;
    }

    public void setCreateAccessKeyResult(CreateAccessKeyResult createAccessKeyResult) {
        this.createAccessKeyResult = createAccessKeyResult;
    }
}

