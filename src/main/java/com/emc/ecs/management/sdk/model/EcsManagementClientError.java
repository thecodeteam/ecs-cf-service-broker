package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class EcsManagementClientError {
	private long code;
	private String description;
	private String details;
	private Boolean retryable;

	public EcsManagementClientError() {
		super();
	}

	public long getCode() {
		return code;
	}

	public void setCode(long code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Boolean getRetryable() {
		return retryable;
	}

	public void setRetryable(Boolean retryable) {
		this.retryable = retryable;
	}

	@Override
	public String toString() {
		return "Error: " + description + " (" + code + "): " + details
				+ ". Retryable: " + retryable + ".";
	}
}