package com.emc.ecs.managementClient.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tags")
public class TagList {
	private List<String> tags;

	public TagList() {
		super();
	}

	@XmlElement(name = "tag")
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

}
