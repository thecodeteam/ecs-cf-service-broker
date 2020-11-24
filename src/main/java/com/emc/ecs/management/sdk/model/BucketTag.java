package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Tag")
@XmlAccessorType(XmlAccessType.FIELD)
public class BucketTag {

    @XmlElement(name = "Key")
    private String key;

    @XmlElement(name = "Value")
    private String value;

    public BucketTag() {
        super();
    }

    public BucketTag(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ':' + value;
    }
}
