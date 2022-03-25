package com.emc.ecs.management.sdk.model.iam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseIamResponse {
    private ResponseMetadata responseMetadata;

    @XmlElementRef
    public ResponseMetadata getResponseMetadata() {
        return responseMetadata;
    }

    public void setResponseMetadata(ResponseMetadata responseMetadata) {
        this.responseMetadata = responseMetadata;
    }

    public void setResponseMetadata(String requestId) {
        this.responseMetadata = new ResponseMetadata(requestId);
    }
}
