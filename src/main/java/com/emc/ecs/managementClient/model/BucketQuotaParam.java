package com.emc.ecs.managementClient.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bucket_quota_param")
public class BucketQuotaParam extends QuotaDetails {

	private String namespace;

	public BucketQuotaParam() {
		super();
	}

	public BucketQuotaParam(String namespace, long blockSize,
			long notificationSize) {
		super();
		this.namespace = namespace;
		setBlockSize(blockSize);
		setNotificationSize(notificationSize);
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}