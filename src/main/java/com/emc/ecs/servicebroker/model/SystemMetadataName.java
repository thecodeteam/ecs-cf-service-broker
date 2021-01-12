package com.emc.ecs.servicebroker.model;

public enum SystemMetadataName {
    CreateTime(SearchMetadataDataType.DateTime),
    LastModified(SearchMetadataDataType.DateTime),
    ObjectName(SearchMetadataDataType.String),
    Owner(SearchMetadataDataType.String),
    Size(SearchMetadataDataType.Integer),
    ContentType(SearchMetadataDataType.String),
    Expiration(SearchMetadataDataType.DateTime),
    ContentEnding(SearchMetadataDataType.String),
    Expires(SearchMetadataDataType.DateTime),
    Retention(SearchMetadataDataType.Integer);

    private SearchMetadataDataType dataType;

    SystemMetadataName(SearchMetadataDataType dataType) {
        this.dataType = dataType;
    }

    public SearchMetadataDataType getDataType() {
        return dataType;
    }

    public static boolean isSystemMetadata(String name) {
        for (SystemMetadataName systemMetadata : values()) {
            if (systemMetadata.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static SystemMetadataName getSystemMetadataName(String name) {
        for (SystemMetadataName systemMetadata : values()) {
            if (systemMetadata.name().equals(name)) {
                return systemMetadata;
            }
        }
        return null;
    }
}
