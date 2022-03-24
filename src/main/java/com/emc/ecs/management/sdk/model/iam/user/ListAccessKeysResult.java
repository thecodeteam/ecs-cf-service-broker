package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ListUserResult
 */

@XmlRootElement(name = "ListAccessKeysResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListAccessKeysResult {

    @JsonProperty(value="AccessKeyMetadata")
    @XmlElement(name = "member")
    @XmlElementWrapper(name = "AccessKeyMetadata")
    private List<IamAccessKey> accessKeys = null;

    @JsonProperty(value="IsTruncated")
    @XmlElement(name = "IsTruncated")
    private Boolean isTruncated;

    @XmlElement(name = "Marker")
    @JsonProperty(value = "Marker")
    private String marker;

    public ListAccessKeysResult addAccessKeysItem(IamAccessKey accessKey) {
        if (this.accessKeys == null) {
            this.accessKeys = new ArrayList<IamAccessKey>();
        }
        this.accessKeys.add(accessKey);
        return this;
    }

    public List<IamAccessKey> getAccessKeys() {
        return accessKeys;
    }

    public void setAccessKeys(List<IamAccessKey> accessKeys) {
        this.accessKeys = accessKeys;
    }

    public ListAccessKeysResult withAccessKeys(List<IamAccessKey> accessKeys) {
        this.accessKeys = accessKeys;
        return this;
    }

    /**
     * A flag that indicates whether there are more items to return.
     */
    public Boolean getIsTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public ListAccessKeysResult withIsTruncated(Boolean isTruncated) {
        this.isTruncated = isTruncated;
        return this;
    }

    /**
     * When isTruncated is true, this element needs to be sent in the Marker parameter for subsequent pagination requests.
     */
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public ListAccessKeysResult withMarker(String marker) {
        this.marker = marker;
        return this;
    }
}

