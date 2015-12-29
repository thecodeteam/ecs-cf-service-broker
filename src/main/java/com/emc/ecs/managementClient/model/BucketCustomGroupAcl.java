package com.emc.ecs.managementClient.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customgroup_acl")
public class BucketCustomGroupAcl {

	private String customgroup;
	private List<String> permission;

	public String getCustomgroup() {
		return customgroup;
	}

	public void setCustomgroup(String customgroup) {
		this.customgroup = customgroup;
	}

	public List<String> getPermission() {
		return permission;
	}

	public void setPermission(List<String> permission) {
		this.permission = permission;
	}

}
