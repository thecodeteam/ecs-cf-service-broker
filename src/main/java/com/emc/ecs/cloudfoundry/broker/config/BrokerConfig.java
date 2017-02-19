package com.emc.ecs.cloudfoundry.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "broker")
public class BrokerConfig {
    private String managementEndpoint;
    private String namespace;
    private String replicationGroup;
    private String baseUrl;
    private String objectEndpoint;
    private String repositoryEndpoint;
    private String repositorySecret;
    private String repositoryServiceId;
    private String repositoryPlanId;
    private String repositoryUser = "user";
    private String username = "root";
    private String password = "ChangeMe";
    private String repositoryBucket = "repository";
    private String prefix = "ecs-cf-broker-";
    private String brokerApiVersion = "2.10";
    private String certificate;

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
        this.repositoryUser = repositoryUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepositoryBucket() {
        return repositoryBucket;
    }

    public void setRepositoryBucket(String repositoryBucket) {
        this.repositoryBucket = repositoryBucket;
    }

    public String getRepositoryEndpoint() {
        if (repositoryEndpoint == null)
            return objectEndpoint;
        return repositoryEndpoint;
    }

    public void setRepositoryEndpoint(String repositoryEndpoint) {
        this.repositoryEndpoint = repositoryEndpoint;
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBrokerApiVersion() {
        return brokerApiVersion;
    }

    public void setBrokerApiVersion(String brokerApiVersion) {
        this.brokerApiVersion = brokerApiVersion;
    }

    public String getRepositorySecret() {
        return repositorySecret;
    }

    public void setRepositorySecret(String repositorySecret) {
        this.repositorySecret = repositorySecret;
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
        this.certificate = certificate;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getObjectEndpoint() {
        return objectEndpoint;
    }

    public void setObjectEndpoint(String objectEndpoint) {
        this.objectEndpoint = objectEndpoint;
    }
}