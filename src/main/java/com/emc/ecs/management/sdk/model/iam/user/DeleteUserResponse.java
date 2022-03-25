package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;

import javax.xml.bind.annotation.*;

/**
 * DeleteUserResponse
 */

@XmlRootElement(name = "DeleteUserResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeleteUserResponse extends BaseIamResponse {
}

