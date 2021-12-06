package com.emc.ecs.servicebroker.config;

import com.emc.ecs.servicebroker.model.ReclaimPolicy;
import com.emc.ecs.servicebroker.model.TileSelector;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.emc.ecs.servicebroker.model.Constants.*;

@SuppressWarnings("unused")
@Configuration
@ConfigurationProperties(prefix = "broker")
public class BrokerConfig {
    private String apiType;

    private String managementEndpoint;
    private String namespace;
    private String replicationGroup;
    private String baseUrl;
    private String objectEndpoint;
    private String nfsMountHost;
    private String repositoryEndpoint;
    private boolean useSsl;
    private boolean ignoreSslValidation;

    private String repositorySecret;
    private String repositoryUser = "user";
    private String username = "root";
    private String password = "ChangeMe";
    private String repositoryBucket = "repository";
    private String prefix = "ecs-cf-broker-";
    private String brokerApiVersion = "*";
    private String certificate;
    private String defaultReclaimPolicy = ReclaimPolicy.Fail.name();

    private String objectscaleGatewayEndpoint;
    private String objectstoreManagementEndpoint;
    private String objectstoreS3Endpoint;
    private String accountId;
    private String accessKey;
    private String secretKey;

    private boolean pathStyleAccess = true;   // Path style access for S3 URL, using host style access if false
    private int loginSessionLength = -1;      // Max login session length, in minutes

    private final ObjectMapper objectMapper = new ObjectMapper();

    // TODO: Add deprecation warning for these settings
    private String repositoryServiceId;
    private String repositoryPlanId;

    public void setCertificateSelector(String certificateJson) throws IOException {
        TileSelector selector = objectMapper.readValue(certificateJson, TileSelector.class);

        if (selector.getValue().equals("No")) {
            Map<String, Object> settings = selector.getSelectedOption();

            if (settings.containsKey(CERTIFICATE) && settings.get(CERTIFICATE) != null) {
                setCertificate(settings.get(CERTIFICATE).toString());
            }
        }
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

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

    public String getBrokerApiVersion() {
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

    public boolean getUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public boolean getIgnoreSslValidation() {
        return ignoreSslValidation;
    }

    public void setIgnoreSslValidation(boolean ignoreSslValidation) {
        this.ignoreSslValidation = ignoreSslValidation;
    }

    public String getDefaultReclaimPolicy() {
        return defaultReclaimPolicy;
    }

    public void setDefaultReclaimPolicy(String defaultReclaimPolicy) {
        this.defaultReclaimPolicy = defaultReclaimPolicy;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public int getLoginSessionLength() {
        return loginSessionLength;
    }

    public void setLoginSessionLength(int loginSessionLength) {
        this.loginSessionLength = loginSessionLength;
    }

    public String getObjectscaleGatewayEndpoint() {
        return objectscaleGatewayEndpoint;
    }

    public void setObjectscaleGatewayEndpoint(String objectscaleGatewayEndpoint) {
        this.objectscaleGatewayEndpoint = objectscaleGatewayEndpoint;
    }

    public String getObjectstoreManagementEndpoint() {
        return objectstoreManagementEndpoint;
    }

    public void setObjectstoreManagementEndpoint(String objectstoreManagementEndpoint) {
        this.objectstoreManagementEndpoint = objectstoreManagementEndpoint;
    }

    public String getObjectstoreS3Endpoint() {
        return objectstoreS3Endpoint;
    }

    public void setObjectstoreS3Endpoint(String objectstoreS3Endpoint) {
        this.objectstoreS3Endpoint = objectstoreS3Endpoint;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Map<String, Object> getSettings() {
        Map<String, Object> ret = new HashMap<>();
        ret.put(BASE_URL, getBaseUrl());
        ret.put(USE_SSL, getUseSsl());
        ret.put(REPLICATION_GROUP, getReplicationGroup());
        ret.put(NAMESPACE, getNamespace());
        ret.put(PATH_STYLE_ACCESS, isPathStyleAccess());
        return ret;
    }
}
