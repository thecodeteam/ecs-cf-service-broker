package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "data_service_vpools")
public class DataServiceReplicationGroupList {

    private List<DataServiceReplicationGroup> replicationGroups;

    @XmlElement(name = "data_service_vpool")
    public List<DataServiceReplicationGroup> getReplicationGroups() {
        return replicationGroups;
    }

    public void setReplicationGroups(
            final List<DataServiceReplicationGroup> replicationGroups) {
        this.replicationGroups = replicationGroups;
    }
}