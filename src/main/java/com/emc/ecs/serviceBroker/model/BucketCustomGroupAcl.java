package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customgroup_acl")
public class BucketCustomGroupAcl {
	
	private String customgroup;
	private String permission;

	public String getCustomgroup() {
		return customgroup;
	}

	public void setCustomgroup(String customgroup) {
		this.customgroup = customgroup;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
