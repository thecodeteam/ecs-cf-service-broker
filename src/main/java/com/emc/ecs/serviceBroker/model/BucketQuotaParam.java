package com.emc.ecs.serviceBroker.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bucket_quota_param")
public class BucketQuotaParam {
	
	private long blockSize = 10;
	private long notificationSize = 8;
	private String namespace;
	
	public BucketQuotaParam() {
		super();
	}

	public BucketQuotaParam(String namespace, long blockSize, long notificationSize) {
		super();
		this.namespace = namespace;
		this.blockSize = blockSize;
		this.notificationSize = notificationSize;
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
