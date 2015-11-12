package com.emc.ecs.serviceBroker.model;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ObjectBucket {

	@XmlElement
	private String id;
	
	@XmlElement
	private Boolean inactive;
	
	@XmlElement
	private String name;
	
	@XmlElement
	private ArrayList<String> tags;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getInactive() {
		return inactive;
	}

	public void setInactive(Boolean inactive) {
		this.inactive = inactive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public Map<String, String> toMap() {
		// TODO Auto-generated method stub
		return null;
	}
}
