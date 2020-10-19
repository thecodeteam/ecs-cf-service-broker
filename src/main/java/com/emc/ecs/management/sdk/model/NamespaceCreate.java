package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;

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

        setExternalGroupAdmins((String) params.get(DOMAIN_GROUP_ADMINS));

        setIsEncryptionEnabled((Boolean) params.get(ENCRYPTED));

        setIsComplianceEnabled((Boolean) params.get(COMPLIANCE_ENABLED));

        setIsStaleAllowed((Boolean) params.get(ACCESS_DURING_OUTAGE));

        setDefaultBucketBlockSize((int) params.getOrDefault(DEFAULT_BUCKET_QUOTA, -1));
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}