package com.emc.ecs.servicebroker.service.utils;

import com.emc.ecs.servicebroker.model.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class TagValuesHandler {
    private static final Logger logger = LoggerFactory.getLogger(TagValuesHandler.class);
    private static final Map<String, String> substitutedTags = new HashMap<>() {
        {
            put(CTX_NAMESPACE_PLACEHOLDER, CTX_NAMESPACE);
            put(CTX_CLUSTER_ID_PLACEHOLDER, CTX_CLUSTER_ID);
            put(CTX_INSTANCE_NAME_PLACEHOLDER, CTX_INSTANCE_NAME);
            put(CF_ORG_GUID_PLACEHOLDER, CTX_ORGANIZATION_GUIDE);
            put(CF_ORG_NAME_PLACEHOLDER, CTX_ORGANIZATION_NAME);
            put(CF_SPACE_GUID_PLACEHOLDER, CTX_SPACE_GUID);
            put(CF_SPACE_NAME_PLACEHOLDER, CTX_SPACE_NAME);
            put(CF_INSTANCE_NAME_PLACEHOLDER, CTX_INSTANCE_NAME);
        }
    };

    /**
     * Substitutes placeholder sequences with request context values.
     * <p>
     * If tag value is found to be one of special sequences (see <code>subsitutedTags</code> map), it's being replaced with a value received in request context map.
     * If there's no value found, tag value will be empty.
     * <p>
     * <a href="https://github.com/openservicebrokerapi/servicebroker/blob/master/profile.md#context-object">Context object specification</a> in OSB API docs
     */
    @SuppressWarnings("unchecked")
    public static void substituteContextValues(List<Map<String, String>> tags, Map<String, Object> parameters) {
        Map<String, String> properties = (Map<String, String>) parameters.get(Constants.REQUEST_CONTEXT_VALUES);

        for (Map<String, String> tag : tags) {
            String value = tag.get(VALUE);

            if (shouldSubstitute(value)) {
                if (hasProperties(parameters)) {
                    tag.put(VALUE, getSubstitutedValue(value, properties));
                } else {
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
        return parameters.get(Constants.REQUEST_CONTEXT_VALUES) != null;
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
