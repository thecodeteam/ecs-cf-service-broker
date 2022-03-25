package com.emc.ecs.management.sdk.model.iam.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ListPoliciesResult
 */

@XmlRootElement(name = "ListPoliciesResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListPoliciesResult {
    
    @JsonProperty(value="Policies")
    @XmlElement(name = "member")
    @XmlElementWrapper(name = "Policies")
    private List<IamPolicy> policies = null;

    @JsonProperty(value="IsTruncated")
    @XmlElement(name = "IsTruncated")
    private Boolean isTruncated;

    @XmlElement(name = "Marker")
    @JsonProperty(value = "Marker")
    private String marker;

    public ListPoliciesResult policies(List<IamPolicy> policies) {
        this.policies = policies;
        return this;
    }

    public ListPoliciesResult addPolicytem(IamPolicy policyItem) {
        if (this.policies == null) {
            this.policies = new ArrayList<IamPolicy>();
        }
        this.policies.add(policyItem);
        return this;
    }

    /**
      * Get policies
      * @return policies
      */
  
    public List<IamPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<IamPolicy> policies) {
        this.policies = policies;
    }

    public ListPoliciesResult isTruncated(Boolean isTruncated) {
        this.isTruncated = isTruncated;
        return this;
    }

    /**
      * A flag that indicates whether there are more items to return.
      * @return isTruncated
      */
  
    public Boolean getIsTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    /**
     * When IsTruncated is true, this element is present and contains the value to use for the Marker
     * parameter in a subsequent pagination request.
     */
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

}

