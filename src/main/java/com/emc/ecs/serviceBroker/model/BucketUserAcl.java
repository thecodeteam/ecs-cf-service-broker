package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_acl")
public class BucketUserAcl {
	
	private String user;
	private String permission;
	
		public BucketUserAcl() {
		super();
	}

	public BucketUserAcl(String user, String permission) {
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

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
