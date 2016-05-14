package com.emc.ecs.managementClient.model;

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

    public String getNamespace() {
	return namespace;
    }

    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }
}