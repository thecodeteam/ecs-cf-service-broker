package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Tag")
@XmlAccessorType(XmlAccessType.FIELD)
public class BucketTag {

    private String Key;
    private String Value;

    public BucketTag() {
        super();
    }

    public BucketTag(String key, String value) {
        super();
        Key = key;
        Value = value;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
