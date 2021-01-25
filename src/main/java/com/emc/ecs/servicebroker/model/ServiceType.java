package com.emc.ecs.servicebroker.model;

import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.SERVICE_TYPE;

public enum ServiceType {
    NAMESPACE("namespace"),
    BUCKET("bucket");

    public String alias;

    ServiceType(String alias) {
        this.alias = alias;
    }

    public static ServiceType fromSettings(Map<String, Object> settings) {
        String v = (String) settings.get(SERVICE_TYPE);

        for (ServiceType type : values()) {
            if (type.alias.equalsIgnoreCase(v)) {
                return type;
            }
        }

        return ServiceType.valueOf(v);
    }

    public String getAlias() {
        return alias;
    }
}
