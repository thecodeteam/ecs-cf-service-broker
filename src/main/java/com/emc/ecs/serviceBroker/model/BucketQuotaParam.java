package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bucket_quota_param")
public class BucketQuotaParam {
	
	@XmlElement
	private long blockSize = 10;
	
	@XmlElement
	private long notificationSize = 8;
	
	@XmlElement
	private String namespace;

	public BucketQuotaParam(String namespace) {
		super();
		this.namespace = namespace;
	}

	public long getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}

	public long getNotificationSize() {
		return notificationSize;
	}

	public void setNotificationSize(long notificationSize) {
		this.notificationSize = notificationSize;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
