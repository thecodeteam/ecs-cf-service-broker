package com.emc.ecs.management.sdk.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_create_param")
public class UserCreateParam {
	private String user;
	private String namespace;
	private List<String> tags;
	
	public UserCreateParam() {
		super();
	}

	public UserCreateParam(String user, String namespace) {
		super();
		this.user = user;
		this.namespace = namespace;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

}