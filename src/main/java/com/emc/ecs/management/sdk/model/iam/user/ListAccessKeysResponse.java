package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.ResponseMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListAccessKeysResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ListAccessKeysResponse extends BaseIamResponse {

    @JsonProperty(value="ListAccessKeysResult")
    @XmlElement(name = "ListAccessKeysResult")
    private ListAccessKeysResult listAccessKeysResult = null;

    public ListAccessKeysResponse withListAccessKeysResult(ListAccessKeysResult result) {
        this.listAccessKeysResult = result;
        return this;
    }

    public ListAccessKeysResult getListAccessKeysResult() {
        return listAccessKeysResult;
    }

    public void setListAccessKeysResult(ListAccessKeysResult result) {
        this.listAccessKeysResult = result;
    }

    public ListAccessKeysResponse responseMetadata(ResponseMetadata responseMetadata) {
        this.setResponseMetadata(responseMetadata);
        return this;
    }

}

