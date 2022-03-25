package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GetAccessKeyLastUsedResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class GetAccessKeyLastUsedResponse extends BaseIamResponse {

    @JsonProperty(value="GetAccessKeyLastUsedResult")
    @XmlElement(name = "GetAccessKeyLastUsedResult")
    private GetAccessKeyLastUsedResult getAccessKeyLastUsedResult = null;

    public GetAccessKeyLastUsedResponse withGetAccessKeyLastUsed(GetAccessKeyLastUsedResult result) {
        this.getAccessKeyLastUsedResult = result;
        return this;
    }

    public GetAccessKeyLastUsedResult getAccessKeyLastUsedResult() {
        return getAccessKeyLastUsedResult;
    }

    public void setGetAccessKeyLastUsedResult(GetAccessKeyLastUsedResult result) {
        this.getAccessKeyLastUsedResult = result;
    }
}

