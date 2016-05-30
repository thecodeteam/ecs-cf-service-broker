package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_delete_param")
public class UserDeleteParam {

	private String user;
	private String namespace;

	public UserDeleteParam() {
		super();
	}

	public UserDeleteParam(String user) {
		super();
		this.user = user;
	}

	public UserDeleteParam(String user, String namespace) {
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

}
