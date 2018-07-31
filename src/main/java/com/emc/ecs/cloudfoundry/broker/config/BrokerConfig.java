package com.emc.ecs.cloudfoundry.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
@Configuration
@ConfigurationProperties(prefix = "broker")
public class BrokerConfig {
    private String managementEndpoint;
    private String namespace;
    private String replicationGroup;
    private String baseUrl;
    private String objectEndpoint;
    private String nfsMountHost;
    private String repositoryEndpoint;

    private String repositorySecret;
    private String repositoryUser = "user";
    private String username = "root";
    private String password = "ChangeMe";
    private String repositoryBucket = "repository";
    private String prefix = "ecs-cf-broker-";
    private String brokerApiVersion = "*";
    private String certificate;
    // TODO: Add deprecation warning for these settings
    private String repositoryServiceId;
    private String repositoryPlanId;

    public String getManagementEndpoint() {
        return managementEndpoint;
    }

    public void setManagementEndpoint(String managementEndpoint) {
        this.managementEndpoint = managementEndpoint;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getReplicationGroup() {
        return replicationGroup;
    }

    public void setReplicationGroup(String replicationGroup) {
        this.replicationGroup = replicationGroup;
    }

    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        if (!repositoryUser.equals(""))
            this.repositoryUser = repositoryUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (!username.equals(""))
            this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (!password.equals(""))
        this.password = password;
    }

    public String getRepositoryBucket() {
        return repositoryBucket;
    }

    public void setRepositoryBucket(String repositoryBucket) {
        if (!repositoryBucket.equals(""))
            this.repositoryBucket = repositoryBucket;
    }

    public String getRepositoryEndpoint() {
        if (repositoryEndpoint == null)
            return objectEndpoint;
        return repositoryEndpoint;
    }

    public void setRepositoryEndpoint(String repositoryEndpoint) {
        if (!repositoryEndpoint.equals(""))
            this.repositoryEndpoint = repositoryEndpoint;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (!prefix.equals(""))
            this.prefix = prefix;
    }

    String getBrokerApiVersion() {
        return brokerApiVersion;
    }

    public void setBrokerApiVersion(String brokerApiVersion) {
        if (!brokerApiVersion.equals(""))
            this.brokerApiVersion = brokerApiVersion;
    }

    public void setRepositorySecret(String repositorySecret) {
        this.repositorySecret = repositorySecret;
    }

    public String getRepositorySecret() {
        return repositorySecret;
    }

    public String getPrefixedBucketName() {
        return prefix + repositoryBucket;
    }

    public String getPrefixedUserName() {
        return prefix + repositoryUser;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        if (!certificate.equals(""))
            this.certificate = certificate;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (!baseUrl.equals(""))
            this.baseUrl = baseUrl;
    }

    public String getObjectEndpoint() {
        return objectEndpoint;
    }

    public void setObjectEndpoint(String objectEndpoint) {
        if (!objectEndpoint.equals(""))
            this.objectEndpoint = objectEndpoint;
    }

    public String getNfsMountHost() {
        return nfsMountHost;
    }

    public void setNfsMountHost(String nfsMountHost) {
        if (!nfsMountHost.equals(""))
            this.nfsMountHost = nfsMountHost;
    }

    public String getRepositoryServiceId() {
        return repositoryServiceId;
    }

    public void setRepositoryServiceId(String repositoryServiceId) {
        this.repositoryServiceId = repositoryServiceId;
    }

    public String getRepositoryPlanId() {
        return repositoryPlanId;
    }

    public void setRepositoryPlanId(String repositoryPlanId) {
        this.repositoryPlanId = repositoryPlanId;
    }

}