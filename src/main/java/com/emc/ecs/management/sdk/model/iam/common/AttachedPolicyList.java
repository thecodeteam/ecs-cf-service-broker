package com.emc.ecs.management.sdk.model.iam.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class AttachedPolicyList extends AbstractIamPagedEntity {

    private List<AttachedPolicy> attachedPolicies;

    @XmlElementWrapper(name = "AttachedPolicies")
    @XmlElement(name = "member")
    @JsonProperty("AttachedPolicies")
    public List<AttachedPolicy> getAttachedPolicies() {
        return attachedPolicies;
    }

    public void setAttachedPolicies(List<AttachedPolicy> attachedPolicies) {
        this.attachedPolicies = attachedPolicies;
    }
}
