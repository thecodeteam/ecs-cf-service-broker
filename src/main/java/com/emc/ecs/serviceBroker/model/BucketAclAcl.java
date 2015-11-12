package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "acl")
public class BucketAclAcl {

	@XmlElement
	private BucketUserAcl userAcl;

	@XmlElement
	private BucketGroupAcl groupAcl;
	
	@XmlElement
	private BucketCustomGroupAcl customGroupAcl;
	
	public BucketUserAcl getUserAcl() {
		return userAcl;
	}

	public void setUserAcl(BucketUserAcl userAcl) {
		this.userAcl = userAcl;
	}

	public BucketGroupAcl getGroupAcl() {
		return groupAcl;
	}

	public void setGroupAcl(BucketGroupAcl groupAcl) {
		this.groupAcl = groupAcl;
	}

	public BucketCustomGroupAcl getCustomGroupAcl() {
		return customGroupAcl;
	}

	public void setCustomGroupAcl(BucketCustomGroupAcl customGroupAcl) {
		this.customGroupAcl = customGroupAcl;
	}
	
}