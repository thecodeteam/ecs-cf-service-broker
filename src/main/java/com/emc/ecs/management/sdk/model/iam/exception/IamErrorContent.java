package com.emc.ecs.management.sdk.model.iam.exception;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"type", "code", "message"})
public class IamErrorContent {
    private String type;
    private String code;
    private String message;

    public IamErrorContent() {
    }

    public IamErrorContent(String type, String code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }

    @XmlElement(name = "Type")
    public String getType() {
        return type;
    }

    @XmlElement(name = "Code")
    public String getCode() {
        return code;
    }

    @XmlElement(name = "Message")
    public String getMessage() {
        return message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "IamErrorContent{" +
                "type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
