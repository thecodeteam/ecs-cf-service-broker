package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;

/**
 * ListUserResult
 */

@XmlRootElement(name = "ListUsersResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListUsersResult {
    
    @JsonProperty(value="Users")
    @XmlElement(name = "member")
    @XmlElementWrapper(name = "Users")
    private List<IamUser> users = null;

    @JsonProperty(value="IsTruncated")
    @XmlElement(name = "IsTruncated")
    private Boolean isTruncated;

    @XmlElement(name = "Marker")
    @JsonProperty(value = "Marker")
    private String marker;

    public ListUsersResult users(List<IamUser> users) {
        this.users = users;
        return this;
    }

    public ListUsersResult addUsersItem(IamUser usersItem) {
        if (this.users == null) {
            this.users = new ArrayList<IamUser>();
        }
        this.users.add(usersItem);
        return this;
    }

    /**
      * Get users
      */
    public List<IamUser> getUsers() {
        return users;
    }

    public void setUsers(List<IamUser> users) {
        this.users = users;
    }

    public ListUsersResult isTruncated(Boolean isTruncated) {
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
     * When isTruncated is true, this element needs to be sent in the Marker parameter for subsequent pagination requests.
     */
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }
}

