package com.emc.ecs.management.sdk.model.iam.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class AbstractIamPagedEntity {

    private String marker;

    @XmlElement(name = "IsTruncated")
    @JsonProperty(value = "IsTruncated")
    public Boolean getTruncated() {
        return Objects.nonNull(marker) && !marker.isEmpty();
    }
    /**
     *
     * When IsTruncated is true, this element is present and contains the value to use for the Marker parameter in a subsequent pagination request.
     */
    @XmlElement(name = "Marker")
    @JsonProperty(value = "Marker")
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }
}
