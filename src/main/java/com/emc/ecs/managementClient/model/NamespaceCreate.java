package com.emc.ecs.managementClient.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "namespace_create")
public class NamespaceCreate extends NamespaceModel {
    private String namespace;

    public NamespaceCreate() {
	super();
    }

    public NamespaceCreate(String namespace, String namespaceAdmins,
	    String replicationGroupURI) {
	super();
	this.namespace = namespace;
	this.setNamespaceAdmins(namespaceAdmins);
	this.setDefaultDataServicesVpool(replicationGroupURI);
	this.setAllowedVpoolsList(replicationGroupURI);
    }
    
    public NamespaceCreate(String namespace, String replicationGroupURI,
	    Map<String, Object> params) {
	this.namespace = namespace;
	setDefaultDataServicesVpool(replicationGroupURI);
	setAllowedVpoolsList(replicationGroupURI);
	setExternalGroupNames(
		(String) params.get("domain-group-admins"));
	setIsEncryptionEnabled(
		(Boolean) params.get("encrypted"));
	setIsComplianceEnabled(
		(Boolean) params.get("compliance-enabled"));
	setIsStaleAllowed(
		(Boolean) params.get("access-during-outage"));
    }

    public String getNamespace() {
	return namespace;
    }

    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }
}