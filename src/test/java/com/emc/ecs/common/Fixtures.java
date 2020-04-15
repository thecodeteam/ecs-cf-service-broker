package com.emc.ecs.common;

import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import org.springframework.cloud.servicebroker.model.binding.*;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;

import java.util.*;

public class Fixtures {
    private static final String QUOTA = "quota";
    private static final String WARN = "warn";
    private static final String LIMIT = "limit";
    private static final String UNLIMITED = "Unlimited";
    private static final String ONE_YEAR = "one-year";
    private static final int ONE_YEAR_IN_SECS = 31536000;
    private static final String FREE_TRIAL = "Free Trial";
    private static final String PAY_PER_GB_PER_MONTH = "Pay per GB Per Month";
    private static final String _5GB = "5gb";
    public static final String GET = "get";
    private static final String RETENTION = "retention";
    public static final String NAMESPACE = "ns1";
    public static final String BASE_URL_ID =
            "urn:ObjectBaseUrl:1b828e4c-b9aa-4c89-915f-d92717b479d2";
    public static final String BASE_URL_NAME = "MyBaseURL";
    public static final String DEFAULT_BASE_URL_NAME = "DefaultBaseURL";
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
    private static final String NAMESPACE_PLAN_ID3 =
            "92ac58fa-6236-4c4f-aab7-9b5f332b487e";
    public static final String BUCKET_PLAN_ID1 =
            "5161888f-d985-40f6-a9bb-48d91c5805c0";
    public static final String BUCKET_PLAN_ID2 =
            "2bc4898b-99aa-491b-9c15-7e10a161bd9f";
    public static final String TEST = "test";
    public static final String EXTERNAL_ADMIN = "group1@foo.com";
    private static final String APP_GUID =
            "eb92048d-6d84-42e0-a293-0b604e53bc6f";
    public static final String BINDING_ID =
            "cf2f8326-3465-4810-9da1-54d328935b81";
    public static final String BUCKET_NAME = "testbucket1";
    private static final String ACCESS_DURING_OUTAGE = "access-during-outage";
    private static final String ENCRYPTED = "encrypted";
    public static final String FILE_ACCESSIBLE = "file-accessible";
    private static final String COMPLIANCE_ENABLED = "compliance-enabled";
    private static final String DOMAIN_GROUP_ADMINS = "domain-group-admins";
    public static final String SERVICE_INSTANCE_ID = "service-instance-id";
    public static final String REMOTE_CONNECT_KEY = "95cb87f5-80d3-48b7-b860-072aeae4a918";
    public static final String EXPORT_NAME = "/export/dir";
    public static final String VOLUME_MOUNT = "/mount/dir";
    public static final String SECRET_KEY = "6b056992-a14a-4fd1-a642-f44a821a7755";
    public static final String REMOTE_CONNECTION = "remote_connection";
    public static final String SHOULD_RAISE_AN_EXCEPTION = "should raise an exception";

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
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .serviceInstanceId(NAMESPACE)
                .build();
    }

    public static CreateServiceInstanceRequest remoteNamespaceCreateRequestFixture(Map<String, Object> params) {
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .parameters(params)
                .serviceInstanceId(NAMESPACE)
                .build();
    }

    public static CreateServiceInstanceRequest namespaceCreateRequestFixture(Map<String, Object> params) {
        return namespaceCreateRequestFixture(SERVICE_INSTANCE_ID, params);
    }

    public static CreateServiceInstanceRequest namespaceCreateRequestFixture(String instanceId, Map<String, Object> params) {
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .parameters(params)
                .serviceInstanceId(instanceId)
                .build();
    }

     public static CreateServiceInstanceRequest bucketCreateRequestFixture(Map<String, Object> params) {
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .parameters(params)
                .serviceInstanceId(BUCKET_NAME)
                .build();
    }

    public static CreateServiceInstanceRequest remoteBucketCreateRequestFixture(Map<String, Object> params) {
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .parameters(params)
                .build();
    }

    public static UpdateServiceInstanceRequest namespaceUpdateRequestFixture(
            Map<String, Object> params) {
        return UpdateServiceInstanceRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .parameters(params)
                .serviceInstanceId(NAMESPACE)
                .build();
    }

    public static UpdateServiceInstanceRequest bucketUpdateRequestFixture(
            Map<String, Object> params) {
        return UpdateServiceInstanceRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .parameters(params)
                .serviceInstanceId(BUCKET_NAME)
                .build();
    }

    public static DeleteServiceInstanceRequest namespaceDeleteRequestFixture() {
        return namespaceDeleteRequestFixture(NAMESPACE);
    }

    public static DeleteServiceInstanceRequest namespaceDeleteRequestFixture(String instanceId) {
        return DeleteServiceInstanceRequest.builder()
            .serviceDefinitionId(NAMESPACE_SERVICE_ID)
            .planId(NAMESPACE_PLAN_ID1)
            .serviceInstanceId(instanceId)
            .build();
    }

    public static DeleteServiceInstanceRequest bucketDeleteRequestFixture() {
        return DeleteServiceInstanceRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .serviceInstanceId(BUCKET_NAME)
                .planId(BUCKET_PLAN_ID1)
                .build();
    }

    public static CreateServiceInstanceBindingRequest namespaceBindingRequestFixture() {
        Map<String, Object> params = new HashMap<>();
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .bindingId(BINDING_ID)
                .bindResource(bindResource)
                .parameters(params)
                .build();
    }

    public static CreateServiceInstanceBindingRequest bucketRemoteConnectFixture() {
        Map<String, Object> params = new HashMap<>();
        params.put("remote_connection", true);
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .bindResource(bindResource)
                .parameters(params)
                .bindingId(BINDING_ID)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .build();
    }

    public static CreateServiceInstanceBindingRequest namespaceRemoteConnectFixture() {
        Map<String, Object> params = new HashMap<>();
        params.put("remote_connection", true);
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .bindResource(bindResource)
                .parameters(params)
                .bindingId(BINDING_ID)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .build();
    }

    public static CreateServiceInstanceBindingRequest bucketBindingPermissionRequestFixture() {
        Map<String, Object> params = new HashMap<>();
        params.put("permissions", Arrays.asList("READ", "WRITE"));
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .bindResource(bindResource)
                .parameters(params)
                .bindingId(BINDING_ID)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .build();
    }

    public static CreateServiceInstanceBindingRequest bucketBindingRequestFixture(Map<String, Object> parameters) {
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .bindResource(bindResource)
                .parameters(parameters)
                .bindingId(BINDING_ID)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .build();
    }

    public static CreateServiceInstanceBindingRequest bucketBindingExportRequestFixture() {
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        Map<String, Object> params = new HashMap<>();
        params.put("mount", VOLUME_MOUNT);
        params.put("export", EXPORT_NAME);
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .parameters(params)
                .bindResource(bindResource)
                .bindingId(BINDING_ID)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .build();
    }

    public static CreateServiceInstanceBindingRequest bucketBindingRequestFixture() {
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();
        Map<String, Object> params = new HashMap<>();
        return CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .planId(BUCKET_PLAN_ID1)
                .bindResource(bindResource)
                .parameters(params)
                .bindingId(BINDING_ID)
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .build();
    }

    public static ServiceInstance serviceInstanceFixture() {
        CreateServiceInstanceRequest createReq = CreateServiceInstanceRequest.builder()
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .serviceDefinitionId("service-one-id")
                .planId("plan-one-id")
                .build();
        return new ServiceInstance(createReq);
    }

    public static ServiceInstanceBinding bindingInstanceFixture() {
        Map<String, Object> creds = new HashMap<>();
        creds.put("accessKey", "user");
        creds.put("bucket", "bucket");
        creds.put("secretKey", "password");
        creds.put("endpoint", OBJ_ENDPOINT);
        Map<String, Object> nested = new HashMap<>();
        nested.put("text2", "zyxwvu");
        nested.put("flag2", true);
        nested.put("number2", 9876);
        Map<String, Object> params = new HashMap<>();
        params.put("number", 1234);
        params.put("flag", true);
        params.put("text", "abcdefg");
        params.put("nested", nested);
        BindResource bindResource = BindResource.builder()
                .appGuid("app-guid")
                .build();
        CreateServiceInstanceBindingRequest createReq = CreateServiceInstanceBindingRequest.builder()
                .planId("plan-one-id")
                .bindResource(bindResource)
                .parameters(params)
                .build();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createReq);
        binding.setBindingId("service-inst-bind-one-id");
        binding.setServiceDefinitionId("service-one-id");
        binding.setCredentials(creds);
        return binding;
    }

    public static ServiceInstanceBinding bindingInstanceVolumeMountFixture() {
        Map<String, Object> creds = new HashMap<>();
        creds.put("accessKey", "user");
        creds.put("bucket", "bucket");
        creds.put("secretKey", "password");
        creds.put("endpoint", OBJ_ENDPOINT);

        CreateServiceInstanceBindingRequest createReq = CreateServiceInstanceBindingRequest.builder().build();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createReq);
        binding.setBindingId("service-inst-bind-one-id");
        binding.setCredentials(creds);
        Map<String, Object> opts = new HashMap<>();
        opts.put("source", "nfs://127.0.0.1/ns1/service-inst-id/");
        opts.put("uid", "456");
        List<VolumeMount> mounts = Collections.singletonList(
                new VolumeMount("nfsv3driver", "/var/vcap/data/" + BINDING_ID,
                        VolumeMount.Mode.READ_WRITE, VolumeMount.DeviceType.SHARED,
                        new SharedVolumeDevice("123", opts))
        );
        binding.setVolumeMounts(mounts);
        return binding;
    }

    public static ServiceInstanceBinding bindingRemoteAccessFixture() {
        Map<String, Object> creds = new HashMap<>();
        creds.put("accessKey", "user");
        creds.put("instanceId", "bucket");
        creds.put("secretKey", "password");
        CreateServiceInstanceBindingRequest createReq = CreateServiceInstanceBindingRequest.builder()
                .bindingId("service-inst-bind-one-id")
                .planId("plan-one-id")
                .build();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createReq);
        binding.setCredentials(creds);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("remote_connection", true);
        binding.setParameters(parameters);
        return binding;
    }

    public static DeleteServiceInstanceBindingRequest namespaceBindingRemoveFixture() {
        return DeleteServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .bindingId(BINDING_ID)
                .serviceInstanceId(NAMESPACE)
                .build();
    }

    public static DeleteServiceInstanceBindingRequest bucketBindingRemoveFixture() {
        return DeleteServiceInstanceBindingRequest.builder()
                .serviceInstanceId(SERVICE_INSTANCE_ID)
                .serviceDefinitionId(BUCKET_SERVICE_ID)
                .bindingId(BINDING_ID)
                .planId(BUCKET_PLAN_ID1)
                .build();
    }
}
