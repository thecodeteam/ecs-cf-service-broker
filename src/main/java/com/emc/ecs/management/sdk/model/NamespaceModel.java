package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;

public class NamespaceModel {
    private String defaultObjectProject;
    private String defaultDataServicesVpool;
    private String allowedVpoolsList;
    private String disallowedVpoolsList;
    private String namespaceAdmins;
    private Boolean isEncryptionEnabled;
    private Integer defaultBucketBlockSize = -1;
    private Boolean isStaleAllowed;
    private Boolean isComplianceEnabled;
    private String externalGroupAdmins;
    private UserMapping userMapping;

    @XmlElement(name = "default_object_project")
    public String getDefaultObjectProject() {
        return defaultObjectProject;
    }

    public void setDefaultObjectProject(String defaultObjectProject) {
        this.defaultObjectProject = defaultObjectProject;
    }

    @XmlElement(name = "default_data_services_vpool")
    public String getDefaultDataServicesVpool() {
        return defaultDataServicesVpool;
    }

    public void setDefaultDataServicesVpool(String defaultDataServicesVpool) {
        this.defaultDataServicesVpool = defaultDataServicesVpool;
    }

    @XmlElement(name = "allowed_vpools_list")
    public String getAllowedVpoolsList() {
        return allowedVpoolsList;
    }

    public void setAllowedVpoolsList(String allowedVpoolsList) {
        this.allowedVpoolsList = allowedVpoolsList;
    }

    @XmlElement(name = "disallowed_vpools_list")
    public String getDisallowedVpoolsList() {
        return disallowedVpoolsList;
    }

    public void setDisallowedVpoolsList(String disallowedVpoolsList) {
        this.disallowedVpoolsList = disallowedVpoolsList;
    }

    @XmlElement(name = "namespace_admins")
    public String getNamespaceAdmins() {
        return namespaceAdmins;
    }

    public void setNamespaceAdmins(String namespaceAdmins) {
        this.namespaceAdmins = namespaceAdmins;
    }

    @XmlElement(name = "is_encryption_enabled")
    public Boolean getIsEncryptionEnabled() {
        return isEncryptionEnabled;
    }

    public void setIsEncryptionEnabled(Boolean isEncryptionEnabled) {
        this.isEncryptionEnabled = isEncryptionEnabled;
    }

    @XmlElement(name = "default_bucket_block_size")
    public Integer getDefaultBucketBlockSize() {
        return defaultBucketBlockSize;
    }

    public void setDefaultBucketBlockSize(Integer defaultBucketBlockSize) {
        this.defaultBucketBlockSize = defaultBucketBlockSize;
    }

    @XmlElement(name = "is_stale_allowed")
    public Boolean getIsStaleAllowed() {
        return isStaleAllowed;
    }

    public void setIsStaleAllowed(Boolean isStaleAllowed) {
        this.isStaleAllowed = isStaleAllowed;
    }

    @XmlElement(name = "is_compliance_enabled")
    public Boolean getIsComplianceEnabled() {
        return isComplianceEnabled;
    }

    public void setIsComplianceEnabled(Boolean complianceEnabled) {
        this.isComplianceEnabled = complianceEnabled;
    }

    @XmlElement(name = "external_group_admins")
    public String getExternalGroupAdmins() {
        return externalGroupAdmins;
    }

    public void setExternalGroupAdmins(String externalGroupAdmins) {
        this.externalGroupAdmins = externalGroupAdmins;
    }

    @XmlElement(name = "user_mapping")
    public UserMapping getUserMapping() {
        return userMapping;
    }

    public void setUserMapping(UserMapping userMapping) {
        this.userMapping = userMapping;
    }
}