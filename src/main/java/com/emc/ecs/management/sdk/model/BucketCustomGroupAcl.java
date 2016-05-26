package com.emc.ecs.management.sdk.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customgroup_acl")
public class BucketCustomGroupAcl {

	private String customgroup;
	private List<String> permissions;

	public String getCustomgroup() {
		return customgroup;
	}

	public void setCustomgroup(String customgroup) {
		this.customgroup = customgroup;
	}

	@XmlElement(name = "permission")
	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

}
