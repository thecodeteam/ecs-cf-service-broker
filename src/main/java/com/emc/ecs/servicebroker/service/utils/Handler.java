package com.emc.ecs.servicebroker.service.utils;

import com.emc.ecs.servicebroker.model.Constants;

import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class Handler {

    private static final Map<String, String> substitutedTags = new HashMap<>() {
        {
            put(CTX_NAMESPACE_PLACEHOLDER, NAMESPACE);
            put(CTX_CLUSTER_ID_PLACEHOLDER, CLUSTER_ID);
            put(CTX_INSTANCE_NAME_PLACEHOLDER, INSTANCE_NAME);
            put(CF_ORG_GUID_PLACEHOLDER, ORGANIZATION_GUIDE);
            put(CF_ORG_NAME_PLACEHOLDER, ORGANIZATION_NAME);
            put(CF_SPACE_GUID_PLACEHOLDER, SPACE_GUID);
            put(CF_SPACE_NAME_PLACEHOLDER, SPACE_NAME);
            put(CF_INSTANCE_NAME_PLACEHOLDER, INSTANCE_NAME);
        }
    };

    @SuppressWarnings("unchecked")
    public static void execute(List<Map<String, String>> tags, Map<String, Object> parameters)  {

        Map<String, String> properties = (Map<String, String>) parameters.get(Constants.Properties);

        for (Map<String, String> tag : tags) {
            Map.Entry<String, String> entry = tag.entrySet().iterator().next();
            String value = entry.getValue();
            String key = entry.getKey();

            if (shouldSubstitute(value)) {
                tag.put(key, getSubstitutedValue(value, properties));
            }
        }

    }

    private static boolean shouldSubstitute(String value) {
        return value.startsWith("$");
    }

    private static String getSubstitutedValue(String tagValue, Map<String, String> properties) {
            String substitutedTag = substitutedTags.get(tagValue);

            if(properties.get(substitutedTag) == null) {
                throw new IllegalArgumentException("Unexpected placeholder : " + tagValue);
            }

            return properties.get(substitutedTag);
    }
}
