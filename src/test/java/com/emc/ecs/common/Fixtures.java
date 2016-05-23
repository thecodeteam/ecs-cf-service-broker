package com.emc.ecs.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import com.emc.ecs.serviceBroker.model.PlanProxy;
import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxy;

public class Fixtures {
    public static final String NAMESPACE = "ns1";
    public static final String PREFIX = "ecs-cf-broker-";
    public static final String REPO_BUCKET = "ecs-cf-broker-repository";
    public static final String REPO_USER = "ecs-cf-broker-user";
    public static final String RG_NAME = "rg1";
    public static final String RG_ID = "urn:storageos:ReplicationGroupInfo:2ef0a92d-cf88-4933-90ba-90245aa031b1:global";
    public static final String OBJ_ENDPOINT = "http://127.0.0.1:9020";
    public static final String SERVICE_ID = "09cac1c6-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String PLAN_ID1 = "09cac5b8-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String PLAN_ID2 = "09cac9f3-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String PLAN_ID3 = "92ac58fa-6236-4c4f-aab7-9b5f332b487e";
    public static final String TEST = "test";
    public static final String ORG_ID = "55083e67-f841-4c7e-9a19-2bf4d0cac6b9";
    public static final String SPACE_ID = "305c3c4d-ca6c-435d-b77f-046f8bc70e79";
    public static final String EXTERNAL_ADMIN = "group1@foo.com";

    public static ServiceDefinitionProxy namespaceServiceFixture() {
	/*
	 * Plan 1: 5gb quota with 4gb notification & default bucket quota of
	 * 5 GB
	 */
	Map<String, Object> settings1 = new HashMap<>();
	Map<String, Object> quota = new HashMap<>();
	quota.put("limit", 5);
	quota.put("warn", 4);
	PlanProxy namespacePlan1 = new PlanProxy(PLAN_ID1, "5gb", "Free Trial",
		null, true);
	settings1.put("default-bucket-quota", 5);
	settings1.put("quota", quota);
	namespacePlan1.setServiceSettings(settings1);

	/*
	 * Plan 2: No quota, compliant, encrypted, access-during-outage with
	 * a domain group admin.
	 */
	Map<String, Object> settings2 = new HashMap<>();
	PlanProxy namespacePlan2 = new PlanProxy(PLAN_ID2, "Unlimited",
		"Pay per GB Per Month", null, false);
	settings2.put("encrypted", true);
	settings2.put("domain-group-admins", EXTERNAL_ADMIN);
	settings2.put("compliance-enabled", true);
	settings2.put("access-during-outage", true);
	namespacePlan2.setServiceSettings(settings2);

	/*
	 * Plan 3: No quota, compliance, encrypted, access-during-outage with
	 * one-year retention.
	 */
	Map<String, Object> retention = new HashMap<>();
	retention.put("one-year", 31536000);
	Map<String, Object> settings3 = new HashMap<>();
	PlanProxy namespacePlan3 = new PlanProxy(PLAN_ID3, "Compliant",
		"Pay per GB Per Month", null, false);
	settings3.put("encrypted", true);
	settings3.put("compliance-enabled", true);
	settings3.put("access-during-outage", true);
	settings3.put("retention", retention);
	namespacePlan3.setServiceSettings(settings3);

	List<PlanProxy> plans = Arrays.asList(namespacePlan1, namespacePlan2,
		namespacePlan3);

	List<String> tags = Arrays.asList("ecs-namespace", "object");
	Map<String, Object> serviceSettings = new HashMap<>();
	serviceSettings.put("service-type", "namespace");

	ServiceDefinitionProxy namespaceService = new ServiceDefinitionProxy(
		SERVICE_ID, "ecs-namespace", "ECS Namespace", true, true, tags,
		serviceSettings, null, plans, null, null);
	return namespaceService;
    }

    public static CreateServiceInstanceRequest namespaceCreateRequestFixture(
	    Map<String, Object> params) {
	return new CreateServiceInstanceRequest(SERVICE_ID, PLAN_ID1, ORG_ID,
		SPACE_ID, params).withServiceInstanceId(NAMESPACE);
    }

    public static UpdateServiceInstanceRequest namespaceUpdateRequestFixture(
	    Map<String, Object> params) {
	return new UpdateServiceInstanceRequest(SERVICE_ID, PLAN_ID1, params)
		.withServiceInstanceId(NAMESPACE);
    }

    public static DeleteServiceInstanceRequest namespaceDeleteRequestFixture() {
	return new DeleteServiceInstanceRequest(NAMESPACE, SERVICE_ID, PLAN_ID1,
		null);
    }
}
