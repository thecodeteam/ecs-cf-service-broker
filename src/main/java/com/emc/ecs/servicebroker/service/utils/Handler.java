package com.emc.ecs.servicebroker.service.utils;

import com.emc.ecs.servicebroker.model.Constants;
import com.emc.ecs.servicebroker.service.EcsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class Handler {

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
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
    public static void execute(List<Map<String, String>> tags, Map<String, Object> parameters) {

        Map<String, String> properties = (Map<String, String>) parameters.get(Constants.Properties);

        for (Map<String, String> tag : tags) {
            String value = tag.get(VALUE);

            if (shouldSubstitute(value)) {
                if (hasProperties(parameters)) {
                    tag.put(VALUE, getSubstitutedValue(value, properties));
                }
                else {
                    logger.info("There was no value for tag substitution for tag value:" + value);
                    tag.put(VALUE, "");
                }
            }
        }

    }

    private static boolean shouldSubstitute(String value) {
        return value.startsWith("$");
    }

    private static boolean hasProperties(Map<String, Object> parameters) {
        return parameters.get(Constants.Properties) != null;
    }

    private static String getSubstitutedValue(String tagValue, Map<String, String> properties) {
        String substitutedTag = substitutedTags.get(tagValue);

        if (properties.get(substitutedTag) == null) {
            logger.info("There was no value for tag substitution for tag value:" + tagValue);
            return "";
        }

        return properties.get(substitutedTag);
    }
}
