package com.emc.ecs.servicebroker.model;

public enum SystemMetadataName {
    CreateTime(MetadataDataType.DateTime),
    LastModified(MetadataDataType.DateTime),
    ObjectName(MetadataDataType.String),
    Owner(MetadataDataType.String),
    Size(MetadataDataType.Integer),
    ContentType(MetadataDataType.String),
    Expiration(MetadataDataType.DateTime),
    ContentEnding(MetadataDataType.String),
    Expires(MetadataDataType.DateTime),
    Retention(MetadataDataType.Integer);

    private MetadataDataType dataType;

    SystemMetadataName(MetadataDataType dataType) {
        this.dataType = dataType;
    }

    public MetadataDataType getDataType() {
        return dataType;
    }

    public static SystemMetadataName getSystemMetadataName(String name) {
        SystemMetadataName[] values = SystemMetadataName.values();
        for (SystemMetadataName value: values) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
