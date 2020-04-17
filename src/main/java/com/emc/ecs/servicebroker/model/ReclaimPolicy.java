package com.emc.ecs.servicebroker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum ReclaimPolicy {
    // Attempt to delete but fail if the bucket isn't empty
    Fail,
    // Leave bucket in-tact but remove from repository
    Detach,
    // Delete all Objects before deleting the bucket
    Delete;

    private static String RECLAIM_POLICY = "reclaim-policy";
    private static String ALLOWED_RECLAIM_POLICIES = "allowed-reclaim-policies";

    public static ReclaimPolicy getReclaimPolicy(Map<String, Object> params) {
        if (params.containsKey(RECLAIM_POLICY)) {
            String reclaimPolicy = params.getOrDefault(RECLAIM_POLICY, ReclaimPolicy.Delete).toString();

            return ReclaimPolicy.valueOf(reclaimPolicy);
        }

        // No Reclaim Policy, Fail is the default
        return ReclaimPolicy.Fail;
    }

    public static List<ReclaimPolicy> getAllowedReclaimPolicies(Map<String, Object> params) {
        if (params.containsKey(ALLOWED_RECLAIM_POLICIES)) {
            List<ReclaimPolicy> allowedPolicies = new ArrayList<>();

            for (String reclaimPolicy : params.get(ALLOWED_RECLAIM_POLICIES).toString().split(",")) {
                allowedPolicies.add(ReclaimPolicy.valueOf(reclaimPolicy.trim()));
            }

            return allowedPolicies;
        }

        // If nothing specified then default to only allowing Fail
        return Collections.singletonList(Fail);
    }

    public static boolean isPolicyAllowed(Map<String, Object> params) {
        return getAllowedReclaimPolicies(params).contains(getReclaimPolicy(params));
    }
}
