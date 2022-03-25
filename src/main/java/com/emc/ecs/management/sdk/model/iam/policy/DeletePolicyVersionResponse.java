package com.emc.ecs.management.sdk.model.iam.policy;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeletePolicyVersionResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeletePolicyVersionResponse extends BaseIamResponse {
}

