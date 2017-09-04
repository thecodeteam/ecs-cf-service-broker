package com.emc.ecs.common;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.model.fixture.ServiceInstanceBindingFixture;
import org.springframework.cloud.servicebroker.model.fixture.ServiceInstanceFixture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fixtures {
    public static final String QUOTA = "quota";
    public static final String WARN = "warn";
    public static final String LIMIT = "limit";
    public static final String UNLIMITED = "Unlimited";
    public static final String UNCHECKED = "unchecked";
    public static final String ONE_YEAR = "one-year";
    public static final int ONE_YEAR_IN_SECS = 31536000;
    public static final String FREE_TRIAL = "Free Trial";
    public static final String PAY_PER_GB_PER_MONTH = "Pay per GB Per Month";
    public static final String _5GB = "5gb";
    public static final String GET = "get";
    public static final String RETENTION = "retention";
    public static final String NAMESPACE = "ns1";
    public static final String BASE_URL_ID =
            "urn:ObjectBaseUrl:1b828e4c-b9aa-4c89-915f-d92717b479d2";
    public static final String BASE_URL_NAME = "MyBaseURL";
    public static final String DEFAULT_BASE_URL_NAME = "DefaultBaseURL";
    public static final String BASE_URL = "s3.mydomain.example.com";
    public static final String PREFIX = "ecs-cf-broker-";
    public static final String REPO_BUCKET = "ecs-cf-broker-repository";
    public static final String REPO_USER = "ecs-cf-broker-user";
    public static final String RG_NAME = "rg1";
    public static final String RG_ID =
            "urn:storageos:ReplicationGroupInfo:2ef0a92d-cf88-4933-90ba-90245aa031b1:global";
    public static final String OBJ_ENDPOINT = "http://127.0.0.1:9020";
    public static final String BUCKET_SERVICE_ID =
            "7181a3b7-f06f-4cce-976b-cc5e859850bc";
    public static final String NAMESPACE_SERVICE_ID =
            "09cac1c6-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String NAMESPACE_PLAN_ID1 =
            "09cac5b8-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String NAMESPACE_PLAN_ID2 =
            "09cac9f3-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String NAMESPACE_PLAN_ID3 =
            "92ac58fa-6236-4c4f-aab7-9b5f332b487e";
    public static final String BUCKET_PLAN_ID1 =
            "5161888f-d985-40f6-a9bb-48d91c5805c0";
    public static final String BUCKET_PLAN_ID2 =
            "2bc4898b-99aa-491b-9c15-7e10a161bd9f";
    public static final String BUCKET_PLAN_ID3 =
            "16aab009-1810-4e71-aa9f-78c1c33ee90e";
    public static final String TEST = "test";
    public static final String ORG_ID = "55083e67-f841-4c7e-9a19-2bf4d0cac6b9";
    public static final String SPACE_ID =
            "305c3c4d-ca6c-435d-b77f-046f8bc70e79";
    public static final String EXTERNAL_ADMIN = "group1@foo.com";
    public static final String APP_GUID =
            "eb92048d-6d84-42e0-a293-0b604e53bc6f";
    public static final String BINDING_ID =
            "cf2f8326-3465-4810-9da1-54d328935b81";
    public static final String BUCKET_NAME = "testbucket1";
    public static final String ACCESS_DURING_OUTAGE = "access-during-outage";
    public static final String ENCRYPTED = "encrypted";
    public static final String FILE_ACCESSIBLE = "file-accessible";
    public static final String COMPLIANCE_ENABLED = "compliance-enabled";
    public static final String DOMAIN_GROUP_ADMINS = "domain-group-admins";
    public static final String EXPORT_NAME = "test_export";
    private static final String SERVICE_TYPE = "service-type";

    public static ServiceDefinitionProxy bucketServiceFixture() {
    /*
     * Plan 1: 5gb quota with 4gb notification
	 */
        Map<String, Object> settings1 = new HashMap<>();
        Map<String, Object> quota = new HashMap<>();
        quota.put(LIMIT, 5);
        quota.put(WARN, 4);
        settings1.put(QUOTA, quota);
        PlanProxy bucketPlan1 = new PlanProxy(BUCKET_PLAN_ID1, _5GB,
                FREE_TRIAL, null, true);
        bucketPlan1.setServiceSettings(settings1);

	/*
     * Plan 2: No quota, encrypted, filesystem, access-during-outage.
	 */
        Map<String, Object> settings2 = new HashMap<>();
        PlanProxy bucketPlan2 = new PlanProxy(BUCKET_PLAN_ID2, UNLIMITED,
                PAY_PER_GB_PER_MONTH, null, false);
        settings2.put(ENCRYPTED, true);
        settings2.put(ACCESS_DURING_OUTAGE, true);
        settings2.put(FILE_ACCESSIBLE, true);
        bucketPlan2.setServiceSettings(settings2);

        List<PlanProxy> plans = Arrays.asList(bucketPlan1, bucketPlan2);

        List<String> tags = Arrays.asList("ecs-bucket", "s3", "swift");
        Map<String, Object> serviceSettings = new HashMap<>();
        serviceSettings.put("service-type", "bucket");
        return new ServiceDefinitionProxy(BUCKET_SERVICE_ID, "ecs-bucket",
                "ECS Bucket", true, true, tags, serviceSettings, null, plans,
                null, null);
    }

    public static ServiceDefinitionProxy namespaceServiceFixture() {
	/*
	 * Plan 1: 5gb quota with 4gb notification & default bucket quota of 5
	 * GB
	 */
        Map<String, Object> settings1 = new HashMap<>();
        Map<String, Object> quota = new HashMap<>();
        quota.put(LIMIT, 5);
        quota.put(WARN, 4);
        PlanProxy namespacePlan1 = new PlanProxy(NAMESPACE_PLAN_ID1, _5GB,
                FREE_TRIAL, null, true);
        settings1.put("default-bucket-quota", 5);
        settings1.put(QUOTA, quota);
        namespacePlan1.setServiceSettings(settings1);

	/*
	 * Plan 2: No quota, compliant, encrypted, access-during-outage with a
	 * domain group admin.
	 */
        Map<String, Object> settings2 = new HashMap<>();
        PlanProxy namespacePlan2 = new PlanProxy(NAMESPACE_PLAN_ID2,
                UNLIMITED, PAY_PER_GB_PER_MONTH, null, false);
        settings2.put(ENCRYPTED, true);
        settings2.put(DOMAIN_GROUP_ADMINS, EXTERNAL_ADMIN);
        settings2.put(COMPLIANCE_ENABLED, true);
        settings2.put(ACCESS_DURING_OUTAGE, true);
        namespacePlan2.setServiceSettings(settings2);

	/*
	 * Plan 3: No quota, compliance, encrypted, access-during-outage with
	 * one-year retention.
	 */
        Map<String, Object> retention = new HashMap<>();
        retention.put(ONE_YEAR, ONE_YEAR_IN_SECS);
        Map<String, Object> settings3 = new HashMap<>();
        PlanProxy namespacePlan3 = new PlanProxy(NAMESPACE_PLAN_ID3,
                "Compliant", PAY_PER_GB_PER_MONTH, null, false);
        settings3.put(ENCRYPTED, true);
        settings3.put(COMPLIANCE_ENABLED, true);
        settings3.put(ACCESS_DURING_OUTAGE, true);
        settings3.put(RETENTION, retention);
        namespacePlan3.setServiceSettings(settings3);

        List<PlanProxy> plans = Arrays.asList(namespacePlan1, namespacePlan2,
                namespacePlan3);

        List<String> tags = Arrays.asList("ecs-namespace", "object");
        Map<String, Object> serviceSettings = new HashMap<>();
        serviceSettings.put("service-type", "namespace");

        return new ServiceDefinitionProxy(NAMESPACE_SERVICE_ID, "ecs-namespace",
                "ECS Namespace", true, true, tags, serviceSettings, null, plans,
                null, null);
    }

    public static CreateServiceInstanceRequest namespaceCreateRequestFixture() {
        return new CreateServiceInstanceRequest(NAMESPACE_SERVICE_ID,
                NAMESPACE_PLAN_ID1, ORG_ID, SPACE_ID)
                .withServiceInstanceId(NAMESPACE);
    }

    public static CreateServiceInstanceRequest namespaceCreateRequestFixture(
            Map<String, Object> params) {
        return new CreateServiceInstanceRequest(NAMESPACE_SERVICE_ID,
                NAMESPACE_PLAN_ID1, ORG_ID, SPACE_ID, params)
                .withServiceInstanceId(NAMESPACE);
    }

    public static CreateServiceInstanceRequest bucketCreateRequestFixture(
            Map<String, Object> params) {
        return new CreateServiceInstanceRequest(BUCKET_SERVICE_ID,
                BUCKET_PLAN_ID1, ORG_ID, SPACE_ID, params)
                .withServiceInstanceId(BUCKET_NAME);
    }

    public static UpdateServiceInstanceRequest namespaceUpdateRequestFixture(
            Map<String, Object> params) {
        return new UpdateServiceInstanceRequest(NAMESPACE_SERVICE_ID,
                NAMESPACE_PLAN_ID1, params).withServiceInstanceId(NAMESPACE);
    }

    public static UpdateServiceInstanceRequest bucketUpdateRequestFixture(
            Map<String, Object> params) {
        return new UpdateServiceInstanceRequest(BUCKET_SERVICE_ID,
                BUCKET_PLAN_ID1, params).withServiceInstanceId(BUCKET_NAME);
    }

    public static DeleteServiceInstanceRequest namespaceDeleteRequestFixture() {
        return new DeleteServiceInstanceRequest(NAMESPACE, NAMESPACE_SERVICE_ID,
                NAMESPACE_PLAN_ID1, null);
    }

    public static DeleteServiceInstanceRequest bucketDeleteRequestFixture() {
        return new DeleteServiceInstanceRequest(BUCKET_NAME, BUCKET_SERVICE_ID,
                BUCKET_PLAN_ID1, null);
    }

    public static CreateServiceInstanceBindingRequest namespaceBindingRequestFixture() {
        Map<String, Object> bindResource = new HashMap<>();
        bindResource.put("app_guid", APP_GUID);
        Map<String, Object> params = new HashMap<>();
        return new CreateServiceInstanceBindingRequest(NAMESPACE_SERVICE_ID,
                NAMESPACE_PLAN_ID1, APP_GUID, bindResource, params)
                .withBindingId(BINDING_ID)
                .withServiceInstanceId(NAMESPACE);
    }

    public static CreateServiceInstanceBindingRequest bucketBindingPermissionRequestFixture() {
        Map<String, Object> bindResource = new HashMap<>();
        bindResource.put("app_guid", APP_GUID);
        Map<String, Object> params = new HashMap<>();
        params.put("permissions", Arrays.asList("READ", "WRITE"));
        return new CreateServiceInstanceBindingRequest(BUCKET_SERVICE_ID,
                BUCKET_PLAN_ID1, APP_GUID, bindResource, params)
                .withBindingId(BINDING_ID)
                .withServiceInstanceId(BUCKET_NAME);
    }

    public static CreateServiceInstanceBindingRequest bucketBindingExportRequestFixture() {
        Map<String, Object> bindResource = new HashMap<>();
        bindResource.put("app_guid", APP_GUID);
        Map<String, Object> params = new HashMap<>();
        params.put("export", EXPORT_NAME);
        return new CreateServiceInstanceBindingRequest(BUCKET_SERVICE_ID,
                BUCKET_PLAN_ID1, APP_GUID, bindResource, params)
                .withBindingId(BINDING_ID)
                .withServiceInstanceId(BUCKET_NAME);
    }

    public static CreateServiceInstanceBindingRequest bucketBindingRequestFixture() {
        Map<String, Object> bindResource = new HashMap<>();
        bindResource.put("app_guid", APP_GUID);
        return new CreateServiceInstanceBindingRequest(BUCKET_SERVICE_ID,
                BUCKET_PLAN_ID1, APP_GUID, bindResource, null)
                .withBindingId(BINDING_ID)
                .withServiceInstanceId(BUCKET_NAME);
    }

    public static ServiceInstance serviceInstanceFixture() {
        return new ServiceInstance(ServiceInstanceFixture
                .buildCreateServiceInstanceRequest(false));
    }

    public static ServiceInstanceBinding bindingInstanceFixture()
            throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        Map<String, Object> creds = new HashMap<String, Object>();
        creds.put("accessKey", "user");
        creds.put("bucket", "bucket");
        creds.put("secretKey", "password");
        creds.put("endpoint", OBJ_ENDPOINT);
        ServiceInstanceBinding binding = new ServiceInstanceBinding(
                ServiceInstanceBindingFixture.buildCreateAppBindingRequest());
        binding.setBindingId("service-inst-bind-one-id");
        binding.setCredentials(creds);
        return binding;
    }

    public static DeleteServiceInstanceBindingRequest namespaceBindingRemoveFixture() {
        return new DeleteServiceInstanceBindingRequest(NAMESPACE, BINDING_ID,
                NAMESPACE_SERVICE_ID, NAMESPACE_PLAN_ID1, null);
    }

    public static DeleteServiceInstanceBindingRequest bucketBindingRemoveFixture() {
        return new DeleteServiceInstanceBindingRequest(BUCKET_NAME, BINDING_ID,
                BUCKET_SERVICE_ID, BUCKET_PLAN_ID1, null);
    }
}