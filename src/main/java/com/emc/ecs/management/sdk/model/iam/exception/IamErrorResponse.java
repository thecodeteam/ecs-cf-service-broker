package com.emc.ecs.management.sdk.model.iam.exception;

import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "ErrorResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.NONE)
public class IamErrorResponse {
    private IamErrorContent errorContent;
    private String requestId;

    public IamErrorResponse() {
    }

    @XmlElementRef
    public IamErrorContent getErrorContent() {
        return errorContent;
    }

    @XmlElement(name = "RequestId")
    public String getRequestId() {
        return requestId;
    }

    public void setErrorContent(IamErrorContent errorContent) {
        this.errorContent = errorContent;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
