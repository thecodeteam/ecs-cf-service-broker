package com.emc.ecs.managementClient.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_acl")
public class BucketUserAcl {

	private String user;
	private List<String> permission;

	public BucketUserAcl() {
		super();
	}

	public BucketUserAcl(String user, List<String> permission) {
		super();
		this.user = user;
		this.permission = permission;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<String> getPermission() {
		return permission;
	}

	public void setPermission(List<String> permission) {
		this.permission = permission;
	}

}
