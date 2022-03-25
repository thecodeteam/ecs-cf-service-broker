package com.emc.ecs.management.sdk.model.iam.policy;

import com.emc.ecs.management.sdk.model.iam.common.AbstractIamPagedEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ListPolicyVersionsResult
 */

@XmlRootElement(name = "ListPolicyVersionsResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListPolicyVersionsResult extends AbstractIamPagedEntity {
    
    @JsonProperty(value="Versions")
    @XmlElement(name = "member")
    @XmlElementWrapper(name = "Versions")
    private List<IamPolicyVersion> policyVersions = null;

    public ListPolicyVersionsResult policyVersions(List<IamPolicyVersion> policyVersions) {
        this.policyVersions = policyVersions;
        return this;
    }

    public ListPolicyVersionsResult addPolicytem(IamPolicyVersion versionItem) {
        if (this.policyVersions == null) {
            this.policyVersions = new ArrayList<IamPolicyVersion>();
        }
        this.policyVersions.add(versionItem);
        return this;
    }

    /**
      * Get policyVersions
      * @return policyVersions
      */
  
    public List<IamPolicyVersion> getPolicyVersions() { return policyVersions; }

    public void setPolicyVersions(List<IamPolicyVersion> policyVersions) {
        this.policyVersions = policyVersions;
    }
}

