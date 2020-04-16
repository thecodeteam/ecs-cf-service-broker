package com.emc.ecs.servicebroker.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class ServiceTypeOption {
    @JsonProperty("repository_service")
    private boolean repositoryService;

    @JsonProperty("default_retention")
    private int defaultRetention;

    @JsonProperty("file_accessible")
    private boolean fileAccessible;

    @JsonProperty("head_type")
    private String headType;

    @JsonProperty("encrypted")
    private boolean encrypted;

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(boolean repositoryService) {
        this.repositoryService = repositoryService;
    }

    public int getDefaultRetention() {
        return defaultRetention;
    }

    public void setDefaultRetention(int defaultRetention) {
        this.defaultRetention = defaultRetention;
    }

    public boolean isFileAccessible() {
        return fileAccessible;
    }

    public void setFileAccessible(boolean fileAccessible) {
        this.fileAccessible = fileAccessible;
    }

    public String getHeadType() {
        return headType;
    }

    public void setHeadType(String headType) {
        this.headType = headType;
    }

}

