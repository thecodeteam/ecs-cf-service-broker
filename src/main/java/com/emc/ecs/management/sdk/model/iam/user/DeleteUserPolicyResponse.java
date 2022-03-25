package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DeleteUserResponse
 */

@XmlRootElement(name = "DeleteUserPolicyResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeleteUserPolicyResponse extends BaseIamResponse {
}

