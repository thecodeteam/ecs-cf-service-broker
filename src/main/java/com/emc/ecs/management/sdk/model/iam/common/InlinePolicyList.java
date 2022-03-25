package com.emc.ecs.management.sdk.model.iam.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class InlinePolicyList extends AbstractIamPagedEntity {

    private List<String> inlinePolicyNames;

    /**
     * Name of inline Policy.
     */
    @XmlElementWrapper(name = "PolicyNames")
    @XmlElement(name = "member")
    @JsonProperty("PolicyNames")
    public List<String> getInlinePolicyNames() {
        return inlinePolicyNames;
    }

    public void setInlinePolicyNames(List<String> inlinePolicyNames) {
        this.inlinePolicyNames = inlinePolicyNames;
    }
}