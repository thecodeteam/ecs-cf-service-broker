package com.emc.ecs.management.sdk.model.iam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * An object representing a tag.
 */

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonRootName(value = "member")
public class TagResponse {

    @JsonProperty(value = "Key")
    @XmlElement(name = "Key")
    private String key;

    @JsonProperty(value = "Value")
    @XmlElement(name = "Value")
    private String value;

    public TagResponse withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * The key name used to retrieve the associated value.
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TagResponse withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * The value associated with the current tag.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagResponse tag = (TagResponse) o;
        return Objects.equals(this.getKey(), tag.getKey()) &&
                Objects.equals(this.getValue(), tag.getValue());
    }
}

