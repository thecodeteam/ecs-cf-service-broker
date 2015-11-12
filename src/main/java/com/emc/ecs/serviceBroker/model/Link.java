package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Link {

	private String href;
	private String rel;
	
	@XmlAttribute
	public String getHref() {
		return href;
	}
	
	@XmlAttribute
	public String getRel() {
		return rel;
	}
}