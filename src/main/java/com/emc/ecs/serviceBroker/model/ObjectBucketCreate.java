package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "object_bucket_create")
public class ObjectBucketCreate {
	private String name;
	private String vpool;
	private Boolean filesystemEnabled = false;
	private String headType = "s3";
	private String namespace;
	
	private Boolean isStaleAllowed = true;

	public ObjectBucketCreate(String name, String namespace, String replicationGroup) {
		super();
		this.name = name;
		this.namespace = namespace;
		this.vpool = replicationGroup;
	}
	
	public ObjectBucketCreate() {
		super();
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

	public void setVpool(String replicationGroup) {
		this.vpool = replicationGroup;
	}
	
	@XmlElement(name = "filesystem_enabled")
	public Boolean getFilesystemEnabled() {
		return filesystemEnabled;
	}

	public void setFilesystemEnabled(Boolean filesystemEnabled) {
		this.filesystemEnabled = filesystemEnabled;
	}

	@XmlElement(name = "head_type")
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

	@XmlElement(name = "is_stale_allowed")
	public Boolean getIsStaleAllowed() {
		return isStaleAllowed;
	}

	public void setIsStaleAllowed(Boolean isStaleAllowed) {
		this.isStaleAllowed = isStaleAllowed;
	}
}