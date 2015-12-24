package com.emc.ecs.serviceBroker.model;

import org.cloudfoundry.community.servicebroker.model.Plan;
import org.springframework.stereotype.Component;

@Component
public class PlanProxy {
	private String id;
	private String name;
	private String description;
	private PlanMetadataProxy metadata;
	private int quotaLimit = -1;
	private int quotaWarning = -1;

	public PlanProxy() {
		super();
	}

	public PlanProxy(String id, String name, String description,
			PlanMetadataProxy metadata) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.metadata = metadata;
	}

	public Plan unproxy() {
		return new Plan(id, name, description, metadata.unproxy());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PlanMetadataProxy getMetadata() {
		return metadata;
	}

	public void setMetadata(PlanMetadataProxy metadata) {
		this.metadata = metadata;
	}

	public int getQuotaLimit() {
		return quotaLimit;
	}

	public void setQuotaLimit(int quotaLimit) {
		this.quotaLimit = quotaLimit;
	}

	public int getQuotaWarning() {
		return quotaWarning;
	}

	public void setQuotaWarning(int quotaWarning) {
		this.quotaWarning = quotaWarning;
	}
}