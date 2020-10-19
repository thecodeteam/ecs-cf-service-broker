package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;

@XmlRootElement(name = "namespace_update")
public class NamespaceUpdate extends NamespaceModel {
    private String vpoolsAddedToAllowedVpoolsList;
    private String vpoolsAddedToDisallowedVpoolsList;
    private String vpoolsRemovedFromAllowedVpoolsList;
    private String vpoolsRemovedFromDisallowedVpoolsList;

    public NamespaceUpdate(Map<String, Object> params) {
        setExternalGroupAdmins((String) params.get(DOMAIN_GROUP_ADMINS));

        // Namespace encryption state cannot be changed after creation
        // setIsEncryptionEnabled((Boolean) params.get(ENCRYPTED));

        setIsComplianceEnabled((Boolean) params.get(COMPLIANCE_ENABLED));

        setIsStaleAllowed((Boolean) params.get(ACCESS_DURING_OUTAGE));

        setDefaultBucketBlockSize((int) params.getOrDefault(DEFAULT_BUCKET_QUOTA, -1));
    }

    public NamespaceUpdate() {
        super();
    }

    @XmlElement(name = "vpools_added_to_allowed_vpools_list")
    public String getVpoolsAddedToAllowedVpoolsList() {
        return vpoolsAddedToAllowedVpoolsList;
    }

    public void setVpoolsAddedToAllowedVpoolsList(
            String vpoolsAddedToAllowedVpoolsList) {
        this.vpoolsAddedToAllowedVpoolsList = vpoolsAddedToAllowedVpoolsList;
    }

    @XmlElement(name = "vpools_added_to_disallowed_vpools_list")
    public String getVpoolsAddedToDisallowedVpoolsList() {
        return vpoolsAddedToDisallowedVpoolsList;
    }

    public void setVpoolsAddedToDisallowedVpoolsList(
            String vpoolsAddedToDisallowedVpoolsList) {
        this.vpoolsAddedToDisallowedVpoolsList = vpoolsAddedToDisallowedVpoolsList;
    }

    @XmlElement(name = "vpools_removed_from_allowed_vpools_list")
    public String getVpoolsRemovedFromAllowedVpoolsList() {
        return vpoolsRemovedFromAllowedVpoolsList;
    }

    public void setVpoolsRemovedFromAllowedVpoolsList(
            String vpoolsRemovedFromAllowedVpoolsList) {
        this.vpoolsRemovedFromAllowedVpoolsList = vpoolsRemovedFromAllowedVpoolsList;
    }

    @XmlElement(name = "vpools_removed_from_disallowed_vpools_list")
    public String getVpoolsRemovedFromDisallowedVpoolsList() {
        return vpoolsRemovedFromDisallowedVpoolsList;
    }

    public void setVpoolsRemovedFromDisallowedVpoolsList(
            String vpoolsRemovedFromDisallowedVpoolsList) {
        this.vpoolsRemovedFromDisallowedVpoolsList = vpoolsRemovedFromDisallowedVpoolsList;
    }
}
