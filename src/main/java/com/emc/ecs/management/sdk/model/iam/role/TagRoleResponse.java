package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TagRoleResponse")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("TagRoleResponse")
public class TagRoleResponse extends BaseIamResponse {
}
