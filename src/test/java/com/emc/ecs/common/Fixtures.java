package com.emc.ecs.common;

import com.emc.ecs.management.sdk.model.BucketTagSetRootElement;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import org.springframework.cloud.servicebroker.model.binding.*;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;

import java.io.File;
import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class Fixtures {
    private static final String UNLIMITED = "Unlimited";
    private static final String ONE_YEAR = "one-year";
    private static final int ONE_YEAR_IN_SECS = 31536000;
    private static final String FREE_TRIAL = "Free Trial";
    private static final String PAY_PER_GB_PER_MONTH = "Pay per GB Per Month";
    private static final String _5GB = "5gb";
    public static final String GET = "get";
    public static final String NAMESPACE_NAME = "ns1";
    public static final String NAMESPACE_NAME_2 = "ns2";
    public static final String BASE_URL_ID = "urn:ObjectBaseUrl:1b828e4c-b9aa-4c89-915f-d92717b479d2";
    public static final String BASE_URL_NAME = "MyBaseURL";
    public static final String DEFAULT_BASE_URL_NAME = "DefaultBaseURL";
    public static final String PREFIX = "ecs-cf-broker-";
    public static final String REPO_BUCKET = "ecs-cf-broker-repository";
    public static final String REPO_USER = "ecs-cf-broker-user";
    public static final String RG_NAME = "rg1";
    public static final String RG_NAME_2 = "rg2";
    public static final String RG_NAME_3 = "rg3";
    public static final String RG_NAME_4 = "rg4";
    public static final String RG_ID = "urn:storageos:ReplicationGroupInfo:2ef0a92d-cf88-4933-90ba-90245aa031b1:global";
    public static final String RG_ID_2 = "urn:storageos:ReplicationGroupInfo:2222d1fb-fdb5-4422-a64a-cf5bcba9d9b2:global";
    public static final String RG_ID_3 = "urn:storageos:ReplicationGroupInfo:333041bc-8668-457b-8e36-5fb8df8ee743:global";
    public static final String RG_ID_4 = "urn:storageos:ReplicationGroupInfo:4444faf5-8aa9-4c5b-a59d-45a870c2de44:global";
    public static final String OBJ_ENDPOINT = "http://127.0.0.1:9020";
    public static final String S3_URL_POSTFIX = ":password@127.0.0.1:9020/bucket";
    public static final String BUCKET_SERVICE_ID = "7181a3b7-f06f-4cce-976b-cc5e859850bc";
    public static final String NAMESPACE_SERVICE_ID = "09cac1c6-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String NAMESPACE_PLAN_ID1 = "09cac5b8-1b0a-11e6-b6ba-3e1d05defe78";
    public static final String NAMESPACE_PLAN_ID2 = "09cac9f3-1b0a-11e6-b6ba-3e1d05defe78";
    private static final String NAMESPACE_PLAN_ID3 = "92ac58fa-6236-4c4f-aab7-9b5f332b487e";
    public static final String BUCKET_PLAN_ID1 = "5161888f-d985-40f6-a9bb-48d91c5805c0";
    public static final String BUCKET_PLAN_ID2 = "2bc4898b-99aa-491b-9c15-7e10a161bd9f";
    public static final String BUCKET_PLAN_ID3 = "3bb6cddf-c977-4213-9784-48f1e8f06a73";
    public static final String TEST = "test";
    public static final String EXTERNAL_ADMIN = "group1@foo.com";
    private static final String APP_GUID = "eb92048d-6d84-42e0-a293-0b604e53bc6f";
    public static final String BINDING_ID = "cf2f8326-3465-4810-9da1-54d328935b81";
    public static final String BUCKET_NAME = "testbucket1";
    public static final String CUSTOM_BUCKET_NAME = "customtestbucket1";
    private static final String ACCESS_DURING_OUTAGE = "access-during-outage";
    public static final String ALLOWED_RECLAIM_POLICIES = "allowed-reclaim-policies";
    public static final String SERVICE_INSTANCE_ID = "service-instance-id";
    public static final String REMOTE_CONNECT_KEY = "95cb87f5-80d3-48b7-b860-072aeae4a918";
    public static final String EXPORT_NAME_VALUE = "/export/dir";
    public static final String VOLUME_MOUNT_VALUE = "/mount/dir";
    public static final String SECRET_KEY_VALUE = "testKEY@ключ:/-s#cr#T";
    public static final String SHOULD_RAISE_AN_EXCEPTION = "should raise an exception";
    public static final String BUCKET_TAGS_INVALID_CHARS = "key?=value!";
    public static final String BUCKET_TAGS_INVALID_FORMAT = "key1:value1;key2:value2";
    public static final String BUCKET_TAGS_LONG_KEY = "very very very very very very very very very very very very very very very very very very very very long key of exactly 129 chars=value";
    public static final String BUCKET_TAGS_LONG_VALUE = "key=very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very great value accurately 257 chars long";
    public static final String KEY1 = "tag1";
    public static final String KEY2 = "tag2";
    public static final String KEY3 = "tag3";
    public static final String VALUE1 = "value1";
    public static final String VALUE2 = "value2";
    public static final String VALUE3 = "value3";
    public static final String BUCKET_TAGS_STRING = KEY1 + "=" + VALUE1 + "," + KEY2 + "=" + VALUE2 + "," + KEY3 + "=" + VALUE3;
    public static final String MARKER = "marker1";
    public static final int PAGE_SIZE = 32;
    public static final String SYSTEM_METADATA_NAME = "Size";
    public static final String SYSTEM_METADATA_TYPE = "Integer";
    public static final String USER_METADATA_NAME = "my_meta";
    public static final String USER_METADATA_TYPE = "String";
    public static final String INVALID_METADATA_TYPE = "invalid_data_type";

    public static ServiceDefinitionProxy bucketServiceFixture() {
        /*
         * Plan 1: 5gb quota with 4gb notification
         */
        Map<String, Object> settings1 = new HashMap<>();
        Map<String, Object> quota = new HashMap<>();
        quota.put(QUOTA_LIMIT, 5);
        quota.put(QUOTA_WARN, 4);
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

        List<String> tags = Arrays.asList("ecs-bucket", HEAD_TYPE_S3, "swift");
        Map<String, Object> serviceSettings = new HashMap<>();
        serviceSettings.put(SERVICE_TYPE, ServiceType.BUCKET.getAlias());
        return new ServiceDefinitionProxy(BUCKET_SERVICE_ID, "ecs-bucket",
                "ECS Bucket", true, true, tags, serviceSettings, null, plans,
                null, null);
    }

    public static ServiceDefinitionProxy bucketServiceWithSettingOverridesFixture() {
        /*
         * Plan 1: QUOTA and BaseUrl name
         */
        PlanProxy bucketPlan1 = new PlanProxy(BUCKET_PLAN_ID1, _5GB, FREE_TRIAL, null, true);
        Map<String, Object> settings1 = new HashMap<>();
        Map<String, Object> quota = new HashMap<>();
        quota.put(QUOTA_LIMIT, 5);
        quota.put(QUOTA_WARN, 4);
        settings1.put(QUOTA, quota);
        settings1.put(BASE_URL, BASE_URL_NAME);
        bucketPlan1.setServiceSettings(settings1);

        /*
         * Plan 2: Replication group, Encrypted
         */
        PlanProxy bucketPlan2 = new PlanProxy(BUCKET_PLAN_ID2, UNLIMITED, PAY_PER_GB_PER_MONTH, null, false);
        Map<String, Object> settings2 = new HashMap<>();
        settings2.put(REPLICATION_GROUP, RG_NAME_2);
        settings2.put(ENCRYPTED, true);
        bucketPlan2.setServiceSettings(settings2);

        /*
         * Plan 3: Namespace
         */
        PlanProxy bucketPlan3 = new PlanProxy(BUCKET_PLAN_ID3, UNLIMITED, PAY_PER_GB_PER_MONTH, null, false);
        Map<String, Object> settings3 = new HashMap<>();
        settings3.put(NAMESPACE, NAMESPACE_NAME_2);
        bucketPlan3.setServiceSettings(settings3);

        List<PlanProxy> plans = Arrays.asList(bucketPlan1, bucketPlan2, bucketPlan3);

        Map<String, Object> serviceSettings = new HashMap<>();
        serviceSettings.put(SERVICE_TYPE, ServiceType.BUCKET.getAlias());
        serviceSettings.put(DEFAULT_RETENTION, 100);
        serviceSettings.put(REPLICATION_GROUP, RG_NAME_3);
        return new ServiceDefinitionProxy(BUCKET_SERVICE_ID, "ecs-bucket",
                "ECS Bucket", true, true, null, serviceSettings, null, plans,
                null, null);
    }


    public static ServiceDefinitionProxy namespaceServiceFixture() {
        /*
         * Plan 1: 5gb quota with 4gb notification & default bucket quota of 5
         * GB
         */
        Map<String, Object> settings1 = new HashMap<>();
        Map<String, Object> quota = new HashMap<>();
        quota.put(QUOTA_LIMIT, 5);
        quota.put(QUOTA_WARN, 4);
        PlanProxy namespacePlan1 = new PlanProxy(NAMESPACE_PLAN_ID1, _5GB, FREE_TRIAL, null, true);
        settings1.put(DEFAULT_BUCKET_QUOTA, 5);
        settings1.put(QUOTA, quota);
        namespacePlan1.setServiceSettings(settings1);

        /*
         * Plan 2: No quota, compliant, encrypted, access-during-outage with a
         * domain group admin.
         */
        Map<String, Object> settings2 = new HashMap<>();
        PlanProxy namespacePlan2 = new PlanProxy(NAMESPACE_PLAN_ID2, UNLIMITED, PAY_PER_GB_PER_MONTH, null, false);
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
        PlanProxy namespacePlan3 = new PlanProxy(NAMESPACE_PLAN_ID3, "Compliant", PAY_PER_GB_PER_MONTH, null, false);
        settings3.put(ENCRYPTED, true);
        settings3.put(COMPLIANCE_ENABLED, true);
        settings3.put(ACCESS_DURING_OUTAGE, true);
        settings3.put(RETENTION, retention);
        namespacePlan3.setServiceSettings(settings3);

        List<PlanProxy> plans = Arrays.asList(namespacePlan1, namespacePlan2,
                namespacePlan3);

        List<String> tags = Arrays.asList("ecs-namespace", "object");
        Map<String, Object> serviceSettings = new HashMap<>();
        serviceSettings.put(SERVICE_TYPE, ServiceType.NAMESPACE.getAlias());

        return new ServiceDefinitionProxy(NAMESPACE_SERVICE_ID, "ecs-namespace",
                "ECS Namespace", true, true, tags, serviceSettings, null, plans,
                null, null);
    }

    public static CreateServiceInstanceRequest namespaceCreateRequestFixture() {
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .serviceInstanceId(NAMESPACE_NAME)
                .build();
    }

    public static CreateServiceInstanceRequest remoteNamespaceCreateRequestFixture(Map<String, Object> params) {
        return CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .parameters(params)
                .serviceInstanceId(NAMESPACE_NAME)
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
                .serviceInstanceId(SERVICE_INSTANCE_ID)
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
        return namespaceDeleteRequestFixture(NAMESPACE_NAME);
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

        params.put(REMOTE_CONNECTION, true);

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

        params.put(REMOTE_CONNECTION, true);

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

        params.put(USER_PERMISSIONS, Arrays.asList("READ", "WRITE"));

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
        Map<String, Object> params = new HashMap<>();

        params.put(VOLUME_MOUNT, VOLUME_MOUNT_VALUE);
        params.put(VOLUME_EXPORT, EXPORT_NAME_VALUE);

        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();

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
        return bucketBindingRequestWithNameFixture(null);
    }

    public static CreateServiceInstanceBindingRequest bucketBindingRequestWithNameFixture(String name) {
        BindResource bindResource = BindResource.builder()
                .appGuid(APP_GUID)
                .build();

        Map<String, Object> params = new HashMap<>();

        if (name != null) {
            params.put(NAME_PARAMETER, name);
        }

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

        ServiceInstance serviceInstance = new ServiceInstance(createReq);

        Map<String, Object> instanceSettings = new HashMap<>();
        instanceSettings.put(NAMESPACE, NAMESPACE_NAME);
        serviceInstance.setServiceSettings(instanceSettings);

        return serviceInstance;
    }

    public static ServiceInstance serviceInstanceWithNameFixture(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put(NAME_PARAMETER, name);

        CreateServiceInstanceRequest createReq = CreateServiceInstanceRequest.builder()
            .serviceInstanceId(SERVICE_INSTANCE_ID)
            .serviceDefinitionId("service-one-id")
            .planId("plan-one-id")
            .parameters(params)
            .build();

        ServiceInstance serviceInstance = new ServiceInstance(createReq);

        Map<String, Object> instanceSettings = new HashMap<>();
        instanceSettings.put(NAMESPACE, NAMESPACE_NAME);
        serviceInstance.setServiceSettings(instanceSettings);

        return serviceInstance;
    }

    public static ServiceInstanceBinding bindingInstanceFixture() {
        return bindingInstanceWithNameFixture(null);
    }

    public static ServiceInstanceBinding bindingInstanceWithNameFixture(String name) {
        Map<String, Object> creds = new HashMap<>();
        creds.put(CREDENTIALS_ACCESS_KEY, "user");
        creds.put(CREDENTIALS_SECRET_KEY, "password");
        creds.put(BUCKET, "bucket");
        creds.put(ENDPOINT, OBJ_ENDPOINT);
        creds.put(S3_URL, "http://" + name + S3_URL_POSTFIX);

        Map<String, Object> nested = new HashMap<>();
        nested.put("text2", "zyxwvu");
        nested.put("flag2", true);
        nested.put("number2", 9876);

        Map<String, Object> params = new HashMap<>();
        params.put("number", 1234);
        params.put("flag", true);
        params.put("text", "abcdefg");
        params.put("nested", nested);

        if (name != null) {
            params.put(NAME_PARAMETER, name);
        }

        BindResource bindResource = BindResource.builder()
                .appGuid("app-guid")
                .build();
        CreateServiceInstanceBindingRequest createReq = CreateServiceInstanceBindingRequest.builder()
                .planId("plan-one-id")
                .bindResource(bindResource)
                .parameters(params)
                .bindingId(BINDING_ID)
                .build();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createReq);
        binding.setBindingId(BINDING_ID);
        binding.setServiceDefinitionId("service-one-id");
        binding.setCredentials(creds);
        return binding;
    }

    public static ServiceInstanceBinding bindingInstanceVolumeMountFixture() {
        Map<String, Object> creds = new HashMap<>();
        creds.put(CREDENTIALS_ACCESS_KEY, "user");
        creds.put(CREDENTIALS_SECRET_KEY, "password");
        creds.put(BUCKET, "bucket");
        creds.put(ENDPOINT, OBJ_ENDPOINT);

        CreateServiceInstanceBindingRequest createReq = CreateServiceInstanceBindingRequest.builder().build();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createReq);
        binding.setBindingId("service-inst-bind-one-id");
        binding.setCredentials(creds);

        Map<String, Object> opts = new HashMap<>();
        opts.put(VOLUME_EXPORT_SOURCE, "nfs://127.0.0.1/ns1/service-inst-id/");
        opts.put(VOLUME_EXPORT_UID, "456");

        List<VolumeMount> mounts = Collections.singletonList(
                new VolumeMount("nfsv3driver", "/var/vcap/data" + File.separator + BINDING_ID,
                        VolumeMount.Mode.READ_WRITE, VolumeMount.DeviceType.SHARED,
                        new SharedVolumeDevice("123", opts))
        );
        binding.setVolumeMounts(mounts);
        return binding;
    }

    public static ServiceInstanceBinding bindingRemoteAccessFixture() {
        Map<String, Object> creds = new HashMap<>();
        creds.put(CREDENTIALS_ACCESS_KEY, "user");
        creds.put(CREDENTIALS_SECRET_KEY, "password");
        creds.put(CREDENTIALS_INSTANCE_ID, "bucket");

        CreateServiceInstanceBindingRequest createReq = CreateServiceInstanceBindingRequest.builder()
                .bindingId("service-inst-bind-one-id")
                .planId("plan-one-id")
                .build();

        ServiceInstanceBinding binding = new ServiceInstanceBinding(createReq);
        binding.setCredentials(creds);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REMOTE_CONNECTION, true);

        binding.setParameters(parameters);

        return binding;
    }

    public static DeleteServiceInstanceBindingRequest namespaceBindingRemoveFixture() {
        return DeleteServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(NAMESPACE_SERVICE_ID)
                .planId(NAMESPACE_PLAN_ID1)
                .bindingId(BINDING_ID)
                .serviceInstanceId(NAMESPACE_NAME)
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

    public static List<Map<String, String>> listOfBucketTagsFixture() {
        List<Map<String, String>> tags = new ArrayList<>();
        Map<String, String> tag1 = new HashMap<>();
        Map<String, String> tag2 = new HashMap<>();
        Map<String, String> tag3 = new HashMap<>();

        tag1.put(BucketTagSetRootElement.KEY, KEY1);
        tag1.put(BucketTagSetRootElement.VALUE, VALUE1);
        tag2.put(BucketTagSetRootElement.KEY, KEY2);
        tag2.put(BucketTagSetRootElement.VALUE, VALUE2);
        tag3.put(BucketTagSetRootElement.KEY, KEY3);
        tag3.put(BucketTagSetRootElement.VALUE, VALUE3);

        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        return tags;
    }
}
