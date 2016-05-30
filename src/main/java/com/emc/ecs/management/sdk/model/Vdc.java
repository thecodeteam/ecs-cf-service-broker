package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "vdc")
public class Vdc {
	private String id;
	private Link link;

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
