package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "default_bucket_retention_update")
public class DefaultBucketRetentionUpdate {
    private String namespace;
    private int period;

    public DefaultBucketRetentionUpdate() {
    }

    public DefaultBucketRetentionUpdate(String namespace, int period) {
        this.namespace = namespace;
        this.period = period;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
