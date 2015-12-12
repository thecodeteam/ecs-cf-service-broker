package com.emc.ecs.managementClient.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bucket_quota_details")
public class BucketQuotaDetails {

	private String bucketname;
	private String namespace;
	private long blockSize;
	private long notificationSize;

	public String getBucketname() {
		return bucketname;
	}

	public void setBucketname(String bucketname) {
		this.bucketname = bucketname;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
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
	
}
