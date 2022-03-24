package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.ResponseMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.*;

/**
 * ListUserResponse
 */

@XmlRootElement(name = "ListUsersResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ListUsersResponse extends BaseIamResponse {
    
    @JsonProperty(value="ListUsersResult")
    @XmlElement(name = "ListUsersResult")
    private ListUsersResult listUsersResult = null;

    public ListUsersResponse listUserResult(ListUsersResult listUsersResult) {
        this.listUsersResult = listUsersResult;
        return this;
    }

    /**
      * Get listUserResult
      * @return listUserResult
      */
  
    public ListUsersResult getListUsersResult() {
        return listUsersResult;
    }

    public void setListUsersResult(ListUsersResult listUsersResult) {
        this.listUsersResult = listUsersResult;
    }

    public ListUsersResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

