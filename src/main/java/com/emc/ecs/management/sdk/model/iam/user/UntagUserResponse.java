package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "UntagUserResponse")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("UntagUserResponse")
public class UntagUserResponse extends BaseIamResponse {
}
