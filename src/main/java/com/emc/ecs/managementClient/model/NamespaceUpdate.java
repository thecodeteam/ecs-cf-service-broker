package com.emc.ecs.managementClient.model;

class NamespaceUpdate extends NamespaceModel {
	private String vpoolsAddedToAllowedVpoolsList;
	private String vpoolsAddedToDisallowedVpoolsList;
	private String vpoolsRemovedFromAllowedVpoolsList;
	private String vpoolsRemovedFromDisallowedVpoolsList;

	public NamespaceUpdate() {
		super();
	}

	public String getVpoolsAddedToAllowedVpoolsList() {
		return vpoolsAddedToAllowedVpoolsList;
	}

	public void setVpoolsAddedToAllowedVpoolsList(String vpoolsAddedToAllowedVpoolsList) {
		this.vpoolsAddedToAllowedVpoolsList = vpoolsAddedToAllowedVpoolsList;
	}

	public String getVpoolsAddedToDisallowedVpoolsList() {
		return vpoolsAddedToDisallowedVpoolsList;
	}

	public void setVpoolsAddedToDisallowedVpoolsList(String vpoolsAddedToDisallowedVpoolsList) {
		this.vpoolsAddedToDisallowedVpoolsList = vpoolsAddedToDisallowedVpoolsList;
	}

	public String getVpoolsRemovedFromAllowedVpoolsList() {
		return vpoolsRemovedFromAllowedVpoolsList;
	}

	public void setVpoolsRemovedFromAllowedVpoolsList(String vpoolsRemovedFromAllowedVpoolsList) {
		this.vpoolsRemovedFromAllowedVpoolsList = vpoolsRemovedFromAllowedVpoolsList;
	}

	public String getVpoolsRemovedFromDisallowedVpoolsList() {
		return vpoolsRemovedFromDisallowedVpoolsList;
	}

	public void setVpoolsRemovedFromDisallowedVpoolsList(String vpoolsRemovedFromDisallowedVpoolsList) {
		this.vpoolsRemovedFromDisallowedVpoolsList = vpoolsRemovedFromDisallowedVpoolsList;
	}	
}
