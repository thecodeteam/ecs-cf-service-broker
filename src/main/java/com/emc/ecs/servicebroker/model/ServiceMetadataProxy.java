package com.emc.ecs.servicebroker.model;

import org.springframework.cloud.servicebroker.autoconfigure.web.ServiceMetadata;

import java.util.Map;

public class ServiceMetadataProxy {
    private String displayName;
    private String documentationUrl;
    private String imageUrl;
    private String imageUrlResource;
    private String longDescription;
    private Map<String, Object> properties;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrlResource() {
        return imageUrlResource;
    }

    public void setImageUrlResource(String imageUrlResource) {
        this.imageUrlResource = imageUrlResource;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }

    public void setProviderDisplayName(String providerDisplayName) {
        this.providerDisplayName = providerDisplayName;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    private String providerDisplayName;
    private String supportUrl;

    public ServiceMetadata unproxy() {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setDisplayName(displayName);
        serviceMetadata.setDocumentationUrl(documentationUrl);
        serviceMetadata.setImageUrl(imageUrl);
        serviceMetadata.setImageUrlResource(imageUrlResource);
        serviceMetadata.setLongDescription(longDescription);
        serviceMetadata.setProperties(properties);
        serviceMetadata.setProviderDisplayName(providerDisplayName);
        serviceMetadata.setSupportUrl(supportUrl);
        return serviceMetadata;
    }
}
