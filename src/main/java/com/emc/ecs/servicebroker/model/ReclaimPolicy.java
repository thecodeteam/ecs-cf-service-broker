package com.emc.ecs.servicebroker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;

public enum ReclaimPolicy {
    // Attempt to delete but fail if the bucket isn't empty
    Fail,
    // Leave bucket in-tact but remove from repository
    Detach,
    // Delete all Objects before deleting the bucket
    Delete;

    public static ReclaimPolicy DEFAULT_RECLAIM_POLICY = Fail;

    public static ReclaimPolicy getReclaimPolicy(Map<String, Object> params) {
        if (params != null && params.containsKey(RECLAIM_POLICY)) {
            String reclaimPolicy = params.getOrDefault(RECLAIM_POLICY, ReclaimPolicy.Delete).toString();

            return ReclaimPolicy.valueOf(reclaimPolicy);
        }

        // No Explict ReclaimPolicy specified
        return DEFAULT_RECLAIM_POLICY;
    }

    public static List<ReclaimPolicy> getAllowedReclaimPolicies(Map<String, Object> params) {
        if (params != null && params.containsKey(ALLOWED_RECLAIM_POLICIES)) {
            List<ReclaimPolicy> allowedPolicies = new ArrayList<>();

            for (String reclaimPolicy : params.get(ALLOWED_RECLAIM_POLICIES).toString().split(",")) {
                allowedPolicies.add(ReclaimPolicy.valueOf(reclaimPolicy.trim()));
            }

            return allowedPolicies;
        }

        // No explicit Allowed Reclaim Policies Specified
        return Collections.singletonList(DEFAULT_RECLAIM_POLICY);
    }

    public static boolean isPolicyAllowed(Map<String, Object> params) {
        return getAllowedReclaimPolicies(params).contains(getReclaimPolicy(params));
    }
}
