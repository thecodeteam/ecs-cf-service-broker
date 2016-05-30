package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bucket_acl")
public class BucketAcl {

	private String bucket;
	private String namespace;
	private String permission;
	private BucketAclAcl acl;

	public BucketAcl() {
		super();
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public BucketAclAcl getAcl() {
		return acl;
	}

	public void setAcl(BucketAclAcl acl) {
		this.acl = acl;
	}

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
