package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "object_bucket_update_is_stale_allowed")
public class BucketAdoUpdate {
    private String namespace;
    private boolean isStaleAllowed;

    public BucketAdoUpdate() {
    }

    public BucketAdoUpdate(String namespace, boolean isStaleAllowed) {
        this.namespace = namespace;
        this.isStaleAllowed = isStaleAllowed;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @XmlElement(name = "is_stale_allowed")
    public boolean getIsStaleAllowed() {
        return isStaleAllowed;
    }

    public void setIsStaleAllowed(boolean isStaleAllowed) {
        this.isStaleAllowed = isStaleAllowed;
    }
}
