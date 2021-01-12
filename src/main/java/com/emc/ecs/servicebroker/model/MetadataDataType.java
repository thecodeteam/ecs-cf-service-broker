package com.emc.ecs.servicebroker.model;

public enum MetadataDataType {
    DateTime,
    Decimal,
    Integer,
    String;

    public static boolean isMetaDataType(String dataType) {
        for (MetadataDataType metadataDataType: MetadataDataType.values()) {
            if (metadataDataType.name().equals(dataType)) {
                return true;
            }
        }
        return false;
    }
}
