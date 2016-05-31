package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "namespace_quota_details")
public class NamespaceQuotaParam extends QuotaDetails {
    private String namespace;

    public NamespaceQuotaParam() {
	super();
    }

    public NamespaceQuotaParam(String namespace, int limit, int warn) {
	this.namespace = namespace;
	setBlockSize(limit);
	setNotificationSize(warn);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
