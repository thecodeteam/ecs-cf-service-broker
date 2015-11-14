package com.emc.ecs.serviceBroker.model;

public class BaseUrl {
	private String name;
	private String id;
	private Link link;

	public BaseUrl() {
		super();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
}
