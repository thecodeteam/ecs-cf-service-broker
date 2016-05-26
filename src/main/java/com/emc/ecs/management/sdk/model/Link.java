package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Link {

	private String href;
	private String rel;
	
	public Link() {
		super();
	}
	
	public Link(String rel, String href) {
		super();
		this.rel = rel;
		this.href = href;
	}

	@XmlAttribute
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@XmlAttribute
	public String getRel() {
		return rel;
	}
	
	public void setRel(String rel) {
		this.rel = rel;
	}
}