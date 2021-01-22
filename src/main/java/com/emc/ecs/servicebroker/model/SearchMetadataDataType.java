package com.emc.ecs.servicebroker.model;

public enum SearchMetadataDataType {
    DateTime,
    Decimal,
    Integer,
    String;

    public static boolean isMetaDataType(String typeName) {
        for (SearchMetadataDataType metadataDataType : values()) {
            if (metadataDataType.name().equals(typeName)) {
                return true;
            }
        }
        return false;
    }
}
