package com.emc.ecs.management.sdk.actions.iam;

import java.util.HashMap;
import java.util.Map;

public class IAMActionUtils {
    public static Map<String, String> accountHeader(String accountId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-emc-namespace", accountId);
        return headers;
    }
}
