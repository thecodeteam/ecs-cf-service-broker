package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bucket_acl")
public class BucketAcl {
	
	@XmlElement
	private String bucket;
	
	@XmlElement
	private String namespace;
	
	@XmlElement
	private String permission;
	
	@XmlElement
	private BucketAclAcl acl;

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
