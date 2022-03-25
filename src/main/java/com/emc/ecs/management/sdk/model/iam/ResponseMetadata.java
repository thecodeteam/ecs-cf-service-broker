package com.emc.ecs.management.sdk.model.iam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ResponseMetadata")
@JsonRootName(value = "ResponseMetadata")
public class ResponseMetadata {
    private String requestId;

    public ResponseMetadata() {
    }

    public ResponseMetadata(String requestId) {
        this.requestId = requestId;
    }

    @XmlElement(name = "RequestId")
    @JsonProperty("RequestId")
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
