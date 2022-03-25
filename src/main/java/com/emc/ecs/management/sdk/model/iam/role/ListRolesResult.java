package com.emc.ecs.management.sdk.model.iam.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "ListRolesResult")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListRolesResult")
public class ListRolesResult {
    private Boolean isTruncated;
    private String marker;
    List<IamRole> roles;

    /**
     * A flag that indicates whether there are more items to return. If your results were truncated, you can make a subsequent pagination request
     * using the Marker request parameter to retrieve more items. Note that IAM might return fewer than the MaxItems number of results even when
     * there are more results available. We recommend that you check IsTruncated after every call to ensure that you receive all your results.
     */
    @XmlElement(name = "IsTruncated")
    @JsonProperty(value = "IsTruncated")
    public Boolean getTruncated() {
        return isTruncated;
    }
    /**
     *
     * When IsTruncated is true, this element is present and contains the value to use for the Marker parameter in a subsequent pagination request.
     */
    @XmlElement(name = "Marker")
    @JsonProperty(value = "Marker")
    public String getMarker() {
        return marker;
    }

    @XmlElementWrapper(name = "Roles")
    @XmlElement(name = "member")
    @JsonProperty("Roles")
    public List<IamRole> getRoles() {
        return roles;
    }

    public void setTruncated(Boolean truncated) {
        isTruncated = truncated;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setRoles(List<IamRole> roles) {
        this.roles = roles;
    }
}
