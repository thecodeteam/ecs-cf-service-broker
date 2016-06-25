package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement(name = "namespace_create")
public class NamespaceCreate extends NamespaceModel {
    private static final String NOADMIN = "-noadmin";
    private String namespace;

    public NamespaceCreate() {
        super();
    }

    public NamespaceCreate(String namespace, String namespaceAdmins,
                           String replicationGroupURI) {
        super();
        this.namespace = namespace;
        this.setNamespaceAdmins(namespace + NOADMIN);
        this.setNamespaceAdmins(namespaceAdmins);
        this.setDefaultDataServicesVpool(replicationGroupURI);
        this.setAllowedVpoolsList(replicationGroupURI);
    }

    public NamespaceCreate(String namespace, String replicationGroupURI,
                           Map<String, Object> params) {
        this.namespace = namespace;
        setDefaultDataServicesVpool(replicationGroupURI);
        setAllowedVpoolsList(replicationGroupURI);
        setExternalGroupAdmins(
                (String) params.get("domain-group-admins"));
        setIsEncryptionEnabled(
                (Boolean) params.get("encrypted"));
        setIsComplianceEnabled(
                (Boolean) params.get("compliance-enabled"));
        setIsStaleAllowed(
                (Boolean) params.get("access-during-outage"));
        setDefaultBucketBlockSize(
                (int) params.getOrDefault("default-bucket-quota", -1));
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}