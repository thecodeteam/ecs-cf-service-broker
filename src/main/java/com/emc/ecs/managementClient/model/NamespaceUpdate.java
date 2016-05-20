package com.emc.ecs.managementClient.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "namespace_update")
public class NamespaceUpdate extends NamespaceModel {
    private String vpoolsAddedToAllowedVpoolsList;
    private String vpoolsAddedToDisallowedVpoolsList;
    private String vpoolsRemovedFromAllowedVpoolsList;
    private String vpoolsRemovedFromDisallowedVpoolsList;

    public NamespaceUpdate(Map<String, Object> params) {
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

    public NamespaceUpdate() {
	super();
    }

    @XmlElement(name = "vpools_added_to_allowed_vpools_list")
    public String getVpoolsAddedToAllowedVpoolsList() {
	return vpoolsAddedToAllowedVpoolsList;
    }

    public void setVpoolsAddedToAllowedVpoolsList(
	    String vpoolsAddedToAllowedVpoolsList) {
	this.vpoolsAddedToAllowedVpoolsList = vpoolsAddedToAllowedVpoolsList;
    }

    @XmlElement(name = "vpools_added_to_disallowed_vpools_list")
    public String getVpoolsAddedToDisallowedVpoolsList() {
	return vpoolsAddedToDisallowedVpoolsList;
    }

    public void setVpoolsAddedToDisallowedVpoolsList(
	    String vpoolsAddedToDisallowedVpoolsList) {
	this.vpoolsAddedToDisallowedVpoolsList = vpoolsAddedToDisallowedVpoolsList;
    }

    @XmlElement(name = "vpools_removed_from_allowed_vpools_list")
    public String getVpoolsRemovedFromAllowedVpoolsList() {
	return vpoolsRemovedFromAllowedVpoolsList;
    }

    public void setVpoolsRemovedFromAllowedVpoolsList(
	    String vpoolsRemovedFromAllowedVpoolsList) {
	this.vpoolsRemovedFromAllowedVpoolsList = vpoolsRemovedFromAllowedVpoolsList;
    }

    @XmlElement(name = "vpools_removed_from_disallowed_vpools_list")
    public String getVpoolsRemovedFromDisallowedVpoolsList() {
	return vpoolsRemovedFromDisallowedVpoolsList;
    }

    public void setVpoolsRemovedFromDisallowedVpoolsList(
	    String vpoolsRemovedFromDisallowedVpoolsList) {
	this.vpoolsRemovedFromDisallowedVpoolsList = vpoolsRemovedFromDisallowedVpoolsList;
    }
}
