package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "object_bucket_create")
public class ObjectBucketCreate {
	@XmlElement
	private String name;

	@XmlElement
	private String vpool;

	@XmlElement(name = "filesystem_enabled")
	private Boolean filesystemEnabled = false;

	@XmlElement(name = "head_type")
	private String headType = "s3";

	@XmlElement
	private String namespace;
	
	@XmlElement(name = "is_stale_allowed")
	private Boolean isStaleAllowed = true;

	public ObjectBucketCreate(String name, String namespace, String vpool) {
		super();
		this.name = name;
		this.namespace = namespace;
		this.vpool = vpool;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVpool() {
		return vpool;
	}

	public void setVpool(String vpool) {
		this.vpool = vpool;
	}

	public Boolean getFilesystemEnabled() {
		return filesystemEnabled;
	}

	public void setFilesystemEnabled(Boolean filesystemEnabled) {
		this.filesystemEnabled = filesystemEnabled;
	}

	public String getHeadType() {
		return headType;
	}

	public void setHeadType(String headType) {
		this.headType = headType;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Boolean getIsStaleAllowed() {
		return isStaleAllowed;
	}

	public void setIsStaleAllowed(Boolean isStaleAllowed) {
		this.isStaleAllowed = isStaleAllowed;
	}
}