package com.emc.ecs.management.sdk.model.iam.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
public class IamAccessKeyLastUsed {

    @JsonProperty(value="LastUsedDate")
    @XmlElement(name = "LastUsedDate")
    private String lastUsedDate;

    @JsonProperty(value="Region")
    @XmlElement(name = "Region")
    private String region = "N/A";

    @JsonProperty(value="ServiceName")
    @XmlElement(name = "ServiceName")
    private String serviceName;

    /**
     * The date and time, in the format of YYYY-MM-DDTHH:mm:ssZ, when the access key was last used.
     * @return lastUsedDate
     */

    public String getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(String lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public IamAccessKeyLastUsed withLastUsedDate(String lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
        return this;
    }

    /**
     * The region
     * @return region
     */

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public IamAccessKeyLastUsed withRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * The name of the service with which this access key was most recently used.
     * @return serviceName
     */

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public IamAccessKeyLastUsed withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

}

