package com.emc.ecs.managementClient.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_create_param")
public class UserCreateParam {
	private String user;
	private String namespace;
	private ArrayList<String> tags;

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

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public UserCreateParam() {
		super();
	}
	
}