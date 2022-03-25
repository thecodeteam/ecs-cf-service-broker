package com.emc.ecs.management.sdk.model.iam.policy;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.ResponseMetadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DeletePolicyResponse
 */

@XmlRootElement(name = "DeletePolicyResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeletePolicyResponse extends BaseIamResponse {

    public DeletePolicyResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }
}

