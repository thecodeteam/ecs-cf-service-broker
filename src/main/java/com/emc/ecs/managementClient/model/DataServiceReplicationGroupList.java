package com.emc.ecs.managementClient.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data_service_vpools")
public class DataServiceReplicationGroupList {
	
	private List<DataServiceReplicationGroup> replicationGroups;

	public DataServiceReplicationGroupList() {
		super();
	}

	@XmlElement(name = "data_service_vpool")
	public List<DataServiceReplicationGroup> getReplicationGroups() {
		return replicationGroups;
	}

	public void setReplicationGroups(final List<DataServiceReplicationGroup> replicationGroups) {
		this.replicationGroups = replicationGroups;
	}
}