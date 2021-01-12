package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.*;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.Constants;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.BucketWipeFactory;
import com.emc.ecs.tool.BucketWipeOperations;
import com.emc.ecs.tool.BucketWipeResult;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.servicebroker.model.Constants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReplicationGroupAction.class, BucketAction.class,
        ObjectUserAction.class, ObjectUserSecretAction.class,
        BaseUrlAction.class, BucketQuotaAction.class,
        BucketRetentionAction.class, NamespaceAction.class,
        NamespaceQuotaAction.class, NamespaceRetentionAction.class,
        BucketAclAction.class, NFSExportAction.class,
        ObjectUserMapAction.class, BucketTagsAction.class})
public class EcsServiceTest {
    private static final String FOO = "foo";
    private static final String ONE_YEAR = "one-year";
    private static final int ONE_YEAR_IN_SECS = 31536000;
    private static final String USER1 = "user1";
    private static final String EXISTS = "exists";
    private static final String REPOSITORY = "repository";
    private static final String USER = "user";
    private static final String DOT = ".";
    private static final String HTTPS = "https://";
    private static final int THIRTY_DAYS_IN_SEC = 2592000;
    private static final String HTTP = "http://";
    private static final String _9020 = ":9020";
    private static final String _9021 = ":9021";
    private static final String THIRTY_DAYS = "thirty-days";
    private static final String UPDATE = "update";
    private static final String CREATE = "create";
    private static final String DELETE = "delete";
    private static final String SOME_OTHER_NAMESPACE_NAME = NAMESPACE_NAME + "_OTHER";

    @Mock
    private Connection connection;

    @Mock
    private BrokerConfig broker;

    @Mock
    private CatalogConfig catalog;

    @Mock
    private BucketWipeFactory bucketWipeFactory;

    @Autowired
    @InjectMocks
    private EcsService ecs;

    private Map<String, Object> brokerSettings = new HashMap<>();

    {
        brokerSettings.put(NAMESPACE, NAMESPACE_NAME);
        brokerSettings.put(REPLICATION_GROUP, RG_NAME);
        brokerSettings.put(BASE_URL, DEFAULT_BASE_URL_NAME);
        brokerSettings.put(USE_SSL, false);
    }

    @Before
    public void setUp() {
        when(broker.getPrefix()).thenReturn(PREFIX);
        when(broker.getReplicationGroup()).thenReturn(RG_NAME);
        when(broker.getNamespace()).thenReturn(NAMESPACE_NAME);
        when(broker.getRepositoryUser()).thenReturn(USER);
        when(broker.getRepositoryBucket()).thenReturn(REPOSITORY);

        when(broker.getSettings()).thenReturn(brokerSettings);
    }

    /**
     * When initializing the ecs-service, and object-endpoint, repo-user &
     * repo-bucket are set, the service will use these static settings. It the
     * repo facilities exist, the ecs-service will continue.
     *
     * @throws EcsManagementClientException po
     */
    @Test
    public void initializeStaticConfigTest() throws EcsManagementClientException {
        setupInitTest();
        when(broker.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);

        ecs.initialize();

        assertEquals(OBJ_ENDPOINT, ecs.getObjectEndpoint());
        assertEquals(PREFIX + "test", ecs.prefix(TEST));
        verify(broker, times(1)).setRepositoryEndpoint(OBJ_ENDPOINT);
        verify(broker, times(1)).setRepositorySecret(TEST);
    }

    /**
     * When initializing the ecs-service, if the object-endpoint is not set
     * statically, but base-url is, the service will look up the endpoint from
     * the base-url.
     *
     * @throws EcsManagementClientException when ECS resources do not exist
     */
    @Test
    public void initializeBaseUrlLookup() throws EcsManagementClientException {
        setupInitTest();
        setupBaseUrlTest(BASE_URL_NAME, false);

        ecs.initialize();
        String objEndpoint = HTTP + BASE_URL + _9020;
        assertEquals(objEndpoint, ecs.getObjectEndpoint());
        verify(broker, times(1)).setRepositoryEndpoint(objEndpoint);
    }

    /**
     * When initializing the ecs-service, if neither the object-endpoint is not
     * set statically nor the base-url, the service will lookup an endpoint
     * named default in the base-url list. If one is found, it will set this as
     * the repo endpoint.
     *
     * @throws EcsManagementClientException when ECS resources do not exist
     */
    @Test
    public void initializeBaseUrlDefaultLookup() throws EcsManagementClientException {
        PowerMockito.mockStatic(ReplicationGroupAction.class);

        setupInitTest();
        setupBaseUrlTest(DEFAULT_BASE_URL_NAME, false);

        ecs.initialize();
        String objEndpoint = HTTP + BASE_URL + _9020;
        assertEquals(objEndpoint, ecs.getObjectEndpoint());
        verify(broker, times(1)).setRepositoryEndpoint(objEndpoint);
    }

    /**
     * When initializing the ecs-service, if neither the object-endpoint is not
     * set statically nor the base-url, the service will lookup an endpoint
     * named default in the base-url list. If none is found, it will throw an
     * exception.
     *
     * @throws EcsManagementClientException when ECS resources do not exist
     */
    @Test(expected = ServiceBrokerException.class)
    public void initializeBaseUrlDefaultLookupFails()
            throws EcsManagementClientException {
        PowerMockito.mockStatic(BaseUrlAction.class);
        when(BaseUrlAction.list(same(connection)))
                .thenReturn(Collections.emptyList());

        ecs.initialize();
    }

    /**
     * When creating a new bucket the settings in the plan will carry through to
     * the created service. Any settings not implemented in the service, the
     * plan or the parameters will be kept as null.
     * <p>
     * The create command will return a map including the resolved service
     * settings.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createBucketDefaultTest() throws Exception {
        setupCreateBucketTest();
        setupCreateBucketQuotaTest(5, 4);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, BUCKET_NAME, service, plan, params);

        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(QUOTA_WARN).longValue());
        assertEquals(5, quota.get(QUOTA_LIMIT).longValue());

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor
                .forClass(ObjectBucketCreate.class);
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());
        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + BUCKET_NAME, create.getName());
        assertNull(create.getIsEncryptionEnabled());
        assertNull(create.getIsStaleAllowed());
        assertEquals(NAMESPACE_NAME, create.getNamespace());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), eq(NAMESPACE_NAME), eq(PREFIX + BUCKET_NAME),
                eq(5), eq(4));
    }

    /**
     * When creating a bucket plan with no user specified parameters, the plan
     * or service settings will be used. The plan service-settings will be
     * observed.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createBucketWithoutParamsTest() throws Exception {
        setupCreateBucketTest();
        PowerMockito.mockStatic(BucketQuotaAction.class);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID2);
        when(catalog.findServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(service);

        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, CUSTOM_BUCKET_NAME, service, plan, new HashMap<>());

        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertTrue((Boolean) serviceSettings.get(FILE_ACCESSIBLE));
        assertNull(serviceSettings.get(QUOTA));

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor.forClass(ObjectBucketCreate.class);

        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());

        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + CUSTOM_BUCKET_NAME, create.getName());
        assertEquals(NAMESPACE_NAME, create.getNamespace());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertTrue(create.getFilesystemEnabled());
        assertEquals(HEAD_TYPE_S3, create.getHeadType());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(0));
        BucketQuotaAction.create(any(Connection.class), anyString(), anyString(), anyInt(), anyInt());

        PowerMockito.verifyStatic(BucketRetentionAction.class, times(0));
        BucketRetentionAction.update(any(Connection.class), anyString(), anyString(), anyInt());
    }

    @Test
    public void createBucketWithParamsTest() throws Exception {
        setupCreateBucketTest();
        setupCreateBucketQuotaTest(5, 4);
        setupBucketRetentionUpdate(100);
        setupChangeBucketTagsTest(null);

        Map<String, Object> additionalParamsQuota = new HashMap<>();
        additionalParamsQuota.put(QUOTA_WARN, 9);
        additionalParamsQuota.put(QUOTA_LIMIT, 10);

        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(QUOTA, additionalParamsQuota);
        additionalParams.put(ENCRYPTED, true);
        additionalParams.put(ACCESS_DURING_OUTAGE, true);
        additionalParams.put(FILE_ACCESSIBLE, true);
        additionalParams.put(DEFAULT_RETENTION, 100);

        List<Map<String, String>> tags = createListOfTags(KEY1, VALUE1, KEY2, VALUE2);
        additionalParams.put(TAGS, tags);

        List<Map<String, String>> searchMetadata = createListOfSearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SYSTEM_METADATA_NAME, SYSTEM_METADATA_TYPE,
                SEARCH_METADATA_TYPE_USER, USER_METADATA_NAME, USER_METADATA_TYPE);
        additionalParams.put(SEARCH_METADATA, searchMetadata);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, CUSTOM_BUCKET_NAME, service, plan, additionalParams);

        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, returnQuota.get(QUOTA_WARN).longValue());
        assertEquals(5, returnQuota.get(QUOTA_LIMIT).longValue());
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertTrue((Boolean) serviceSettings.get(FILE_ACCESSIBLE));

        List<Map<String, String>> setTags = (List<Map<String, String>>) serviceSettings.get(TAGS);
        assertTrue(CollectionUtils.isEqualCollection(tags, setTags));

        List<Map<String, String>> setSearchMetadata = (List<Map<String, String>>) serviceSettings.get(SEARCH_METADATA);
        assertTrue(CollectionUtils.isEqualCollection(searchMetadata, setSearchMetadata));

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor.forClass(ObjectBucketCreate.class);
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());

        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + CUSTOM_BUCKET_NAME, create.getName());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertTrue(create.getFilesystemEnabled());
        assertEquals(NAMESPACE_NAME, create.getNamespace());
        assertEquals(RG_ID, create.getVpool());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), eq(NAMESPACE_NAME), eq(PREFIX + CUSTOM_BUCKET_NAME), eq(5), eq(4));

        PowerMockito.verifyStatic(BucketRetentionAction.class, times(1));
        BucketRetentionAction.update(same(connection), eq(NAMESPACE_NAME), eq(PREFIX + CUSTOM_BUCKET_NAME), eq(100));

        ArgumentCaptor<BucketTagsParamAdd> tagsParamAddCaptor = ArgumentCaptor.forClass(BucketTagsParamAdd.class);
        PowerMockito.verifyStatic(BucketTagsAction.class, times(1));
        BucketTagsAction.create(same(connection), eq(PREFIX + CUSTOM_BUCKET_NAME), tagsParamAddCaptor.capture());
        BucketTagsParamAdd tagsParamAdd = tagsParamAddCaptor.getValue();
        List<Map<String, String>> invokedTags = tagsParamAdd.getTagSetAsListOfTags();
        assertTrue(CollectionUtils.isEqualCollection(tags, invokedTags));
        assertEquals(NAMESPACE_NAME, tagsParamAdd.getNamespace());
    }

    @Test
    public void createBucketInRgAndNsInParamsTest() throws Exception {
        setupCreateBucketTest();
        setupCreateBucketQuotaTest(5, 4);

        Map<String, Object> params = new HashMap<>();
        params.put(ENCRYPTED, true);
        params.put(ACCESS_DURING_OUTAGE, true);

        params.put(NAMESPACE, SOME_OTHER_NAMESPACE_NAME);
        params.put(REPLICATION_GROUP, RG_NAME_2);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, BUCKET_NAME, service, plan, params);

        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, returnQuota.get(QUOTA_WARN).longValue());
        assertEquals(5, returnQuota.get(QUOTA_LIMIT).longValue());
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertNull(serviceSettings.get(FILE_ACCESSIBLE));

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor.forClass(ObjectBucketCreate.class);
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());

        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + BUCKET_NAME, create.getName());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertNull(create.getFilesystemEnabled());

        assertEquals(SOME_OTHER_NAMESPACE_NAME, create.getNamespace());
        assertEquals(RG_ID_2, create.getVpool());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), eq(SOME_OTHER_NAMESPACE_NAME), eq(PREFIX + BUCKET_NAME), eq(5), eq(4));
    }

    /**
     * Buckets are not file enabled by default
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void getBucketFileEnabledTest() throws Exception {
        ObjectBucketInfo fakeBucket = new ObjectBucketInfo();

        PowerMockito.mockStatic(BucketAction.class);
        PowerMockito.when(BucketAction.class, GET,
                same(connection), anyString(), anyString()).thenReturn(fakeBucket);

        boolean isEnabled = ecs.getBucketFileEnabled(FOO, NAMESPACE_NAME);
        assertFalse(isEnabled);

        fakeBucket.setFsAccessEnabled(true);
        isEnabled = ecs.getBucketFileEnabled(FOO, NAMESPACE_NAME);
        assertTrue(isEnabled);
    }

    @Test
    public void wipeBucketTest() throws Exception {
        setupInitTest();
        when(broker.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);

        BucketWipeOperations bucketWipeOperations = mock(BucketWipeOperations.class);
        doNothing().when(bucketWipeOperations).deleteAllObjects(any(), any(), any());

        when(bucketWipeFactory.getBucketWipe(any())).thenReturn(bucketWipeOperations);

        // Setup bucket wipe with a CompletableFuture that never returns
        CompletableFuture wipeCompletableFuture = new CompletableFuture();
        BucketWipeResult bucketWipeResult = mock(BucketWipeResult.class);
        when(bucketWipeResult.getCompletedFuture()).thenReturn(wipeCompletableFuture);
        when(bucketWipeFactory.newBucketWipeResult()).thenReturn(bucketWipeResult);

        // Re-initialize EcsService with the new BucketWipeFactory
        ecs.initialize();

        // Setup Action Static mocks
        PowerMockito.mockStatic(BucketAction.class);
        setupBucketExistsTest();
        setupBucketAclTest();
        setupBucketGetTest();
        setupBucketDeleteTest();

        // Perform Test
        ecs.wipeAndDeleteBucket(BUCKET_NAME, NAMESPACE_NAME);

        // Verify that Bucket Exists Was called correctly
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);

        BucketAction.exists(same(connection), idCaptor.capture(), nsCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());

        // Complete the Delete operation
        wipeCompletableFuture.complete(true);

        // Verify that Delete Bucket was called
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        ArgumentCaptor<String> idCaptor2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor2 = ArgumentCaptor.forClass(String.class);

        BucketAction.delete(same(connection), idCaptor2.capture(), nsCaptor2.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor2.getValue());
    }

    @Test
    public void deleteBucketTest() throws Exception {
        PowerMockito.mockStatic(BucketAction.class);
        setupBucketExistsTest();
        setupBucketDeleteTest();

        // Perform Test
        ecs.deleteBucket(BUCKET_NAME, NAMESPACE_NAME);

        // Verify that Bucket Exists Was called correctly
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);

        BucketAction.exists(same(connection), idCaptor.capture(), nsCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());

        // Verify that Delete Bucket was called
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        ArgumentCaptor<String> idCaptor2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor2 = ArgumentCaptor.forClass(String.class);

        BucketAction.delete(same(connection), idCaptor2.capture(), nsCaptor2.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor2.getValue());

    }

    /**
     * When changing plans from one with a quota to one without a quota any
     * existing quota should be deleted.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeBucketPlanTestNoQuota() throws Exception {
        setupDeleteBucketQuotaTest();
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);
        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID2);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, new HashMap<>(), null);
        assertNull(serviceSettings.get(QUOTA));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.delete(same(connection), nsCaptor.capture(), idCaptor.capture()
        );
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
    }

    /**
     * When changing plans from one without a quota and quota parameters are
     * supplied, the quota parameters must dictate the quota created.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeBucketPlanTestParametersQuota() throws Exception {
        setupCreateBucketQuotaTest(100, 80);
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID2);

        Map<String, Object> quota = new HashMap<>();
        quota.put(QUOTA_LIMIT, 100);
        quota.put(QUOTA_WARN, 80);
        Map<String, Object> params = new HashMap<>();
        params.put(QUOTA, quota);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, params, null);
        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(80, returnQuota.get(QUOTA_WARN).longValue());
        assertEquals(100, returnQuota.get(QUOTA_LIMIT).longValue());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor
                .forClass(Integer.class);
        ArgumentCaptor<Integer> warnCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), nsCaptor.capture(), idCaptor.capture(),
                limitCaptor.capture(),
                warnCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(Integer.valueOf(100), limitCaptor.getValue());
        assertEquals(Integer.valueOf(80), warnCaptor.getValue());
    }

    /**
     * When changing plans from one with a quota and quota parameters are
     * supplied, the quota parameters must be ignored.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeBucketPlanTestParametersIgnoredQuota() throws Exception {
        setupCreateBucketQuotaTest(5, 4);
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);
        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> quota = new HashMap<>();
        quota.put(QUOTA_LIMIT, 100);
        quota.put(QUOTA_WARN, 80);
        Map<String, Object> params = new HashMap<>();
        params.put(QUOTA, quota);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, params, null);
        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, returnQuota.get(QUOTA_WARN).longValue());
        assertEquals(5, returnQuota.get(QUOTA_LIMIT).longValue());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor
                .forClass(Integer.class);
        ArgumentCaptor<Integer> warnCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), nsCaptor.capture(), idCaptor.capture(),
                limitCaptor.capture(),
                warnCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(Integer.valueOf(5), limitCaptor.getValue());
        assertEquals(Integer.valueOf(4), warnCaptor.getValue());
    }

    /**
     * When changing plans from one without a quota to one with a quota the new
     * quota should be created.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeBucketPlanTestNewQuota() throws Exception {
        setupCreateBucketQuotaTest(5, 4);
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, new HashMap<>(), null);
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(QUOTA_WARN).longValue());
        assertEquals(5, quota.get(QUOTA_LIMIT).longValue());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor
                .forClass(Integer.class);
        ArgumentCaptor<Integer> warnCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), nsCaptor.capture(), idCaptor.capture(),
                limitCaptor.capture(),
                warnCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(Integer.valueOf(5), limitCaptor.getValue());
        assertEquals(Integer.valueOf(4), warnCaptor.getValue());
    }

    /**
     * When changing plans from one with a default retention period provided
     * in parameters it should be updated.
     */
    @Test
    public void changeBucketPlanTestParametersRetention() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(DEFAULT_RETENTION, THIRTY_DAYS_IN_SEC);
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC + 100);
        setupDeleteBucketQuotaTest();

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, params, new HashMap<>());
        assertEquals(THIRTY_DAYS_IN_SEC, serviceSettings.get(DEFAULT_RETENTION));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> periodCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketRetentionAction.class, times(1));
        BucketRetentionAction.update(same(connection), nsCaptor.capture(),
                idCaptor.capture(), periodCaptor.capture());
        assertEquals(NAMESPACE_NAME, PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(Integer.valueOf(THIRTY_DAYS_IN_SEC), periodCaptor.getValue());
    }

    /**
     * When changing plans from one with no retention period provided
     * in parameters it should be changed to zero.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeBucketPlanTestNoRetention() throws Exception {
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);
        setupDeleteBucketQuotaTest();

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, new HashMap<>(), new HashMap<>());
        assertEquals(0, serviceSettings.get(DEFAULT_RETENTION));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> periodCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketRetentionAction.class, times(1));
        BucketRetentionAction.update(same(connection), nsCaptor.capture(),
                idCaptor.capture(), periodCaptor.capture());
        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(Integer.valueOf(0), periodCaptor.getValue());
    }

    /**
     * When changing plans from one with some tags provided in parameters
     * all existing tags should stay the same, all new tags should be added to the bucket
     * and all repetitive tags should be ignored.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeBucketPlanTestTags() throws Exception {
        List<Map<String, String>> currentTags = createListOfTags(KEY2, VALUE2, KEY3, VALUE3);

        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);
        setupDeleteBucketQuotaTest();
        setupChangeBucketTagsTest(currentTags);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> params = new HashMap<>();
        params.put(TAGS, createListOfTags(KEY2, VALUE1, KEY1, VALUE1));

        List<Map<String, String>> expectedTags = createListOfTags(KEY1, VALUE1, KEY2, VALUE1, KEY3, VALUE3);
        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, params, null);
        List<Map<String, String>> setTags = (List<Map<String, String>>) serviceSettings.get(TAGS);
        assertTrue(expectedTags.size() == setTags.size() && expectedTags.containsAll(setTags) && setTags.containsAll(expectedTags));
    }

    /**
     * A service must be able to remove a user from a bucket.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void removeUserFromBucketTest() throws Exception {
        BucketAcl bucketAcl = new BucketAcl();
        BucketUserAcl userAcl = new BucketUserAcl(PREFIX + USER1, FULL_CONTROL);
        BucketAclAcl acl = new BucketAclAcl();
        acl.setUserAccessList(Collections.singletonList(userAcl));
        bucketAcl.setAcl(acl);

        PowerMockito.mockStatic(BucketAclAction.class);
        PowerMockito
                .when(BucketAclAction.class, GET,
                        same(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE_NAME))
                .thenReturn(bucketAcl);
        PowerMockito.doNothing()
                .when(BucketAclAction.class, UPDATE,
                        same(connection), eq(PREFIX + BUCKET_NAME), any(BucketAcl.class));
        PowerMockito
                .when(BucketAclAction.class, EXISTS,
                        same(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE_NAME))
                .thenReturn(true);

        ecs.removeUserFromBucket(BUCKET_NAME, NAMESPACE_NAME, USER1);

        PowerMockito.verifyStatic(BucketAclAction.class);
        BucketAclAction.exists(eq(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE_NAME));
        BucketAclAction.get(eq(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE_NAME));
        ArgumentCaptor<BucketAcl> aclCaptor = ArgumentCaptor.forClass(BucketAcl.class);
        PowerMockito.verifyStatic(BucketAclAction.class);
        BucketAclAction.update(eq(connection), eq(PREFIX + BUCKET_NAME), aclCaptor.capture());
        List<BucketUserAcl> actualUserAcl = aclCaptor.getValue().getAcl().getUserAccessList();
        assertFalse(actualUserAcl.contains(userAcl));
    }

    /**
     * A service must be able to delete a user.
     *
     * @throws EcsManagementClientException when resources are not found
     */
    @Test
    public void deleteUser() throws Exception {
        PowerMockito.mockStatic(ObjectUserAction.class);
        PowerMockito
                .when(ObjectUserAction.class, EXISTS, same(connection), any(String.class), any(String.class))
                .thenReturn(true);

        ecs.deleteUser(USER1, NAMESPACE_NAME);
        PowerMockito.verifyStatic(ObjectUserAction.class);

        ObjectUserAction.exists(same(connection), eq(PREFIX + USER1), eq(NAMESPACE_NAME));
        ObjectUserAction.delete(same(connection), eq(PREFIX + USER1));
    }

    /**
     * When creating a new namespace the settings in the plan will carry through
     * to the created service. Any settings not implemented in the service, the
     * plan or the parameters will be kept as null.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createNamespaceDefaultTest() throws Exception {
        setupCreateNamespaceTest();
        setupCreateNamespaceQuotaTest();

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(0);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE_NAME, namespaceServiceFixture(), plan, params);
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(QUOTA_WARN).longValue());
        assertEquals(5, quota.get(QUOTA_LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);

        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor
                .forClass(NamespaceCreate.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE_NAME, create.getNamespace());
        assertNull(create.getIsEncryptionEnabled());
        assertNull(create.getIsComplianceEnabled());
        assertNull(create.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), create.getDefaultBucketBlockSize());

        PowerMockito.verifyStatic(NamespaceQuotaAction.class);

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceQuotaParam> quotaParamCaptor = ArgumentCaptor
                .forClass(NamespaceQuotaParam.class);
        NamespaceQuotaAction.create(same(connection), idCaptor.capture(),
                quotaParamCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, idCaptor.getValue());
        assertEquals(5, quotaParamCaptor.getValue().getBlockSize());
        assertEquals(4, quotaParamCaptor.getValue().getNotificationSize());
    }

    /**
     * When changing plans the params of the new plan should be set, on the
     * existing namespace. The resulting update action will have the settings of
     * the new plan.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeNamespacePlanTest() throws Exception {
        setupUpdateNamespaceTest();

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceUpdate> updateCaptor = ArgumentCaptor.forClass(NamespaceUpdate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID2);
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE_NAME, service, plan, params);
        assertNull(serviceSettings.get(QUOTA));

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.update(same(connection), idCaptor.capture(), updateCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, idCaptor.getValue());
        NamespaceUpdate update = updateCaptor.getValue();
        assertEquals(EXTERNAL_ADMIN, update.getExternalGroupAdmins());
        assertNull("Namespace encryption state cannot be changed after creation, value should be null", update.getIsEncryptionEnabled());
        assertTrue(update.getIsComplianceEnabled());
        assertTrue(update.getIsStaleAllowed());
    }

    /**
     * When creating a plan with no user specified parameters, the plan or
     * service settings will be used. The default-bucket-quota will be "5".
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createNamespaceWithoutParamsTest() throws Exception {
        setupCreateNamespaceTest();
        setupCreateNamespaceQuotaTest();

        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor.forClass(NamespaceCreate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(0);
        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID)).thenReturn(service);

        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE_NAME, service, plan, new HashMap<>());
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(QUOTA_WARN).longValue());
        assertEquals(5, quota.get(QUOTA_LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE_NAME, create.getNamespace());
        assertEquals(null, create.getExternalGroupAdmins());
        assertEquals(null, create.getIsEncryptionEnabled());
        assertEquals(null, create.getIsComplianceEnabled());
        assertEquals(null, create.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), create.getDefaultBucketBlockSize());
        assertEquals(DEFAULT_BASE_URL_NAME, serviceSettings.get(BASE_URL));
        assertEquals(RG_ID, create.getAllowedVpoolsList());

        PowerMockito.verifyStatic(NamespaceQuotaAction.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceQuotaParam> quotaParamCaptor = ArgumentCaptor.forClass(NamespaceQuotaParam.class);
        NamespaceQuotaAction.create(same(connection), idCaptor.capture(), quotaParamCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, idCaptor.getValue());
        assertEquals(5, quotaParamCaptor.getValue().getBlockSize());
        assertEquals(4, quotaParamCaptor.getValue().getNotificationSize());
    }

    /**
     * When creating plan with user specified parameters, the params should be
     * set, except when overridden by a plan or service settings. Therefore,
     * default-bucket-quota will not be "10", it will be "5" since that's the
     * setting in the plan. Other parameter settings will carry through.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createNamespaceWithParamsTest() throws Exception {
        setupCreateNamespaceTest();
        setupCreateNamespaceQuotaTest();
        setupReplicationGroupsList();

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(DOMAIN_GROUP_ADMINS, EXTERNAL_ADMIN);
        additionalParameters.put(ENCRYPTED, true);
        additionalParameters.put(COMPLIANCE_ENABLED, true);
        additionalParameters.put(ACCESS_DURING_OUTAGE, true);
        additionalParameters.put(DEFAULT_BUCKET_QUOTA, 10);
        additionalParameters.put(BASE_URL, BASE_URL_NAME);
        additionalParameters.put(REPLICATION_GROUP, RG_NAME_2);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(0);

        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID)).thenReturn(service);

        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE_NAME, service, plan, additionalParameters);

        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(COMPLIANCE_ENABLED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertEquals(BASE_URL_NAME, serviceSettings.get(BASE_URL));
        assertEquals(5, serviceSettings.get(DEFAULT_BUCKET_QUOTA));
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(QUOTA_WARN).longValue());
        assertEquals(5, quota.get(QUOTA_LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);
        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor.forClass(NamespaceCreate.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE_NAME, create.getNamespace());
        assertEquals(EXTERNAL_ADMIN, create.getExternalGroupAdmins());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsComplianceEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), create.getDefaultBucketBlockSize());
        assertEquals(RG_ID_2, create.getAllowedVpoolsList());

        PowerMockito.verifyStatic(NamespaceQuotaAction.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceQuotaParam> quotaParamCaptor = ArgumentCaptor.forClass(NamespaceQuotaParam.class);

        NamespaceQuotaAction.create(same(connection), idCaptor.capture(), quotaParamCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, idCaptor.getValue());
        assertEquals(5, quotaParamCaptor.getValue().getBlockSize());
        assertEquals(4, quotaParamCaptor.getValue().getNotificationSize());
    }

    /**
     * When changing namespace plan with user specified parameters, the params
     * should be set, except when overridden by a plan or service setting.
     * Therefore, default-bucket-quota will not be "10", it will be "5" since
     * that's the setting in the plan. Other settings will carry through.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeNamespacePlanWithParamsTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(DOMAIN_GROUP_ADMINS, EXTERNAL_ADMIN);
        params.put(ENCRYPTED, true);
        params.put(COMPLIANCE_ENABLED, true);
        params.put(ACCESS_DURING_OUTAGE, true);
        params.put(DEFAULT_BUCKET_QUOTA, 10);

        setupUpdateNamespaceTest();

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceUpdate> updateCaptor = ArgumentCaptor
                .forClass(NamespaceUpdate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE_NAME, service, plan, params);
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertTrue((Boolean) serviceSettings.get(COMPLIANCE_ENABLED));
        assertEquals(EXTERNAL_ADMIN, serviceSettings.get(DOMAIN_GROUP_ADMINS));
        assertEquals(5, serviceSettings.get(DEFAULT_BUCKET_QUOTA));
        assertEquals(4, quota.get(QUOTA_WARN).longValue());
        assertEquals(5, quota.get(QUOTA_LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.update(same(connection), idCaptor.capture(),
                updateCaptor.capture());
        NamespaceUpdate update = updateCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE_NAME, idCaptor.getValue());
        assertEquals(EXTERNAL_ADMIN, update.getExternalGroupAdmins());
        assertNull("Namespace encryption state cannot be changed after creation, value should be null", update.getIsEncryptionEnabled());
        assertTrue(update.getIsComplianceEnabled());
        assertTrue(update.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), update.getDefaultBucketBlockSize());
    }

    /**
     * When creating a namespace with retention-class defined in the plan and
     * the retention-class does not already exist, the retention class should be
     * created.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createNamespaceWithRetention() throws Exception {
        Map<String, Object> params = new HashMap<>();
        setupUpdateNamespaceTest();
        setupCreateNamespaceRetentionTest(false);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(2);

        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());

        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE_NAME, service, plan, params);
        Map<String, Object> returnRetention = (Map<String, Object>) serviceSettings.get(RETENTION);
        assertEquals(ONE_YEAR_IN_SECS, returnRetention.get(ONE_YEAR));

        PowerMockito.verifyStatic(NamespaceAction.class);
        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor
                .forClass(NamespaceCreate.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE_NAME, create.getNamespace());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertTrue(create.getIsComplianceEnabled());

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);
        ArgumentCaptor<RetentionClassCreate> retentionCreateCaptor = ArgumentCaptor
                .forClass(RetentionClassCreate.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        NamespaceRetentionAction.create(same(connection), idCaptor.capture(),
                retentionCreateCaptor.capture());
        RetentionClassCreate retention = retentionCreateCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE_NAME, idCaptor.getValue());
        assertEquals(ONE_YEAR, retention.getName());
        assertEquals(ONE_YEAR_IN_SECS, retention.getPeriod());
    }

    /**
     * When changing a namespace plan and a different retention-class is
     * specified in the parameters, then the retention class should be added.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeNamespacePlanNewRentention() throws Exception {
        Map<String, Object> retention = new HashMap<>();
        retention.put(THIRTY_DAYS, THIRTY_DAYS_IN_SEC);
        Map<String, Object> params = new HashMap<>();
        params.put(RETENTION, retention);

        setupUpdateNamespaceTest();
        setupCreateNamespaceRetentionTest(false);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RetentionClassCreate> createCaptor = ArgumentCaptor
                .forClass(RetentionClassCreate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID2);
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE_NAME, service, plan, params);
        Map<String, Object> returnRetention = (Map<String, Object>) serviceSettings.get(RETENTION);
        assertEquals(THIRTY_DAYS_IN_SEC, returnRetention.get(THIRTY_DAYS));

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);
        NamespaceRetentionAction.create(same(connection), nsCaptor.capture(),
                createCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(THIRTY_DAYS, createCaptor.getValue().getName());
        assertEquals(THIRTY_DAYS_IN_SEC, createCaptor.getValue().getPeriod());
    }

    /**
     * When changing a namespace plan and a retention-class is specified in the
     * parameters with the value -1, then the retention class should be removed.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeNamespacePlanRemoveRetention() throws Exception {
        Map<String, Object> retention = new HashMap<>();
        retention.put(THIRTY_DAYS, -1);
        Map<String, Object> params = new HashMap<>();
        params.put(RETENTION, retention);

        setupUpdateNamespaceTest();
        setupCreateNamespaceRetentionTest(true);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID2);
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE_NAME, service, plan, params);
        assertNull(serviceSettings.get(RETENTION));

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rcCaptor = ArgumentCaptor.forClass(String.class);
        NamespaceRetentionAction.delete(same(connection), nsCaptor.capture(),
                rcCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(THIRTY_DAYS, rcCaptor.getValue());
    }

    /**
     * When changing a namespace plan and a retention-class is specified in the
     * parameters with a different value than the existing value, then the
     * retention-class should be changed.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void changeNamespacePlanChangeRentention() throws Exception {
        Map<String, Object> retention = new HashMap<>();
        retention.put(THIRTY_DAYS, THIRTY_DAYS_IN_SEC);
        Map<String, Object> params = new HashMap<>();
        params.put(RETENTION, retention);

        setupUpdateNamespaceTest();
        setupCreateNamespaceRetentionTest(true);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rcCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RetentionClassUpdate> updateCaptor = ArgumentCaptor
                .forClass(RetentionClassUpdate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID2);
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE_NAME, service, plan, params);
        Map<String, Object> returnRetention = (Map<String, Object>) serviceSettings.get(RETENTION);
        assertEquals(THIRTY_DAYS_IN_SEC, returnRetention.get(THIRTY_DAYS));

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);
        NamespaceRetentionAction.update(same(connection), nsCaptor.capture(),
                rcCaptor.capture(), updateCaptor.capture());
        assertEquals(PREFIX + NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(THIRTY_DAYS, rcCaptor.getValue());
        assertEquals(THIRTY_DAYS_IN_SEC, updateCaptor.getValue().getPeriod());
    }

    /**
     * When creating a namespace with retention-class defined in the plan and
     * the retention-class does not already exist, the retention class should be
     * created.
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createBucketWithRetention() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(DEFAULT_RETENTION, THIRTY_DAYS_IN_SEC);
        setupCreateBucketTest();
        setupCreateBucketRetentionTest(THIRTY_DAYS_IN_SEC);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(2);

        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());

        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, BUCKET_NAME, service, plan, params);
        assertEquals(THIRTY_DAYS_IN_SEC, serviceSettings.get(DEFAULT_RETENTION));

        PowerMockito.verifyStatic(BucketRetentionAction.class);
        BucketRetentionAction.update(same(connection), eq(NAMESPACE_NAME),
                eq(PREFIX + BUCKET_NAME), eq(THIRTY_DAYS_IN_SEC));
    }

    /**
     * A namespace should be able to be deleted.
     *
     * @throws Exception when mocking fails
     */
    @PrepareForTest({NamespaceAction.class})
    @Test
    public void deleteNamespace() throws Exception {
        setupDeleteNamespaceTest();

        ecs.deleteNamespace(NAMESPACE_NAME);

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.exists(same(connection), eq(PREFIX + NAMESPACE_NAME));
        NamespaceAction.delete(same(connection), eq(PREFIX + NAMESPACE_NAME));
    }

    /**
     * A user can be created within a specific namespace
     *
     * @throws Exception when mocking fails
     */
    @Test
    public void createUserInNamespace() throws Exception {
        PowerMockito.mockStatic(ObjectUserAction.class);
        PowerMockito.doNothing().when(ObjectUserAction.class, CREATE,
                same(connection), anyString(), anyString());

        PowerMockito.mockStatic(ObjectUserSecretAction.class);
        PowerMockito.when(ObjectUserSecretAction.class, CREATE,
                same(connection), anyString()).thenReturn(new UserSecretKey());

        PowerMockito
                .when(ObjectUserSecretAction.class, "list", same(connection),
                        anyString())
                .thenReturn(Collections.singletonList(new UserSecretKey()));

        ecs.createUser(USER1, NAMESPACE_NAME);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userCaptor = ArgumentCaptor
                .forClass(String.class);
        PowerMockito.verifyStatic(ObjectUserAction.class);
        ObjectUserAction.create(same(connection), userCaptor.capture(),
                nsCaptor.capture());
        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(PREFIX + USER1, userCaptor.getValue());
    }

    /**
     * A service can lookup a service definition from the catalog
     */
    @Test
    public void testlookupServiceDefinition() {
        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        ServiceDefinitionProxy service = ecs
                .lookupServiceDefinition(NAMESPACE_SERVICE_ID);
        assertEquals(NAMESPACE_SERVICE_ID, service.getId());
    }

    /**
     * A lookup of a non-existent service definition ID fails
     *
     * @throws ServiceBrokerException when service is not found
     */
    @Test(expected = ServiceBrokerException.class)
    public void testLookupMissingServiceDefinitionFails() throws ServiceBrokerException {
        ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID);
    }

    /**
     * A service can lookup a namespace URL using the default base URL in the
     * broker config without SSL, which is default.
     *
     * @throws EcsManagementClientException when service is not found
     */
    @Test
    public void testNamespaceURLNoSSLDefaultBaseURL() throws EcsManagementClientException {
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        Map<String, Object> serviceSettings = plan.getServiceSettings();
        serviceSettings.putAll(service.getServiceSettings());

        when(broker.getBaseUrl()).thenReturn(DEFAULT_BASE_URL_NAME);

        setupBaseUrlTest(DEFAULT_BASE_URL_NAME, true);

        String expectedUrl = HTTP + NAMESPACE_NAME + DOT + BASE_URL + _9020;
        assertEquals(expectedUrl, ecs.getNamespaceURL(NAMESPACE_NAME, Collections.emptyMap(), serviceSettings));
    }

    /**
     * A service can lookup a namespace URL using the a specific base URL with
     * SSL set in the service.
     *
     * @throws EcsManagementClientException when service is not found
     */
    @Test
    public void testNamespaceURLSSLDefaultBaseURL() throws EcsManagementClientException {
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        Map<String, Object> serviceSettings = service.getServiceSettings();
        serviceSettings.putAll(plan.getServiceSettings());
        serviceSettings.put(USE_SSL, true);
        service.setServiceSettings(serviceSettings);

        when(broker.getBaseUrl()).thenReturn(DEFAULT_BASE_URL_NAME);

        setupBaseUrlTest(DEFAULT_BASE_URL_NAME, true);

        String expectedUrl = HTTPS + NAMESPACE_NAME + DOT + BASE_URL + _9021;
        assertEquals(expectedUrl, ecs.getNamespaceURL(NAMESPACE_NAME, Collections.emptyMap(), serviceSettings));
    }

    /**
     * A service can lookup a namespace URL using the parameter supplied base
     * URL, and without SSL.
     *
     * @throws EcsManagementClientException when service is not found
     */
    @Test
    public void testNamespaceURLNoSSLParamBaseURL() throws EcsManagementClientException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(BASE_URL, BASE_URL_NAME);
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        Map<String, Object> serviceSettings = plan.getServiceSettings();
        serviceSettings.putAll(service.getServiceSettings());

        setupBaseUrlTest(BASE_URL_NAME, true);

        String expectedUrl = HTTP + NAMESPACE_NAME + DOT + BASE_URL + _9020;
        assertEquals(expectedUrl, ecs.getNamespaceURL(NAMESPACE_NAME, params, serviceSettings));
    }

    /**
     * A service can lookup a namespace URL using the parameter supplied base
     * URL, and with SSL.
     */
    @Test
    public void testNamespaceURLSSLParamBaseURL() throws EcsManagementClientException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(BASE_URL, BASE_URL_NAME);
        ServiceDefinitionProxy service = namespaceServiceFixture();
        Map<String, Object> serviceSettings = service.getServiceSettings();
        serviceSettings.put(USE_SSL, true);
        service.setServiceSettings(serviceSettings);
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        serviceSettings.putAll(plan.getServiceSettings());
        serviceSettings.put(USE_SSL, true);

        setupBaseUrlTest(BASE_URL_NAME, true);

        String expectedURl = HTTPS + NAMESPACE_NAME + DOT + BASE_URL + _9021;
        assertEquals(expectedURl, ecs.getNamespaceURL(NAMESPACE_NAME, params, serviceSettings));
    }

    /**
     * A service can add an export to a bucket
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testAddNonexistentExportToBucket() throws Exception {
        String absolutePath = "/" + NAMESPACE_NAME + "/" + PREFIX + BUCKET_NAME + "/" + EXPORT_NAME_VALUE;
        PowerMockito.mockStatic(NFSExportAction.class);

        when(NFSExportAction.list(same(connection), eq(absolutePath)))
                .thenReturn(null);

        PowerMockito.doNothing().when(NFSExportAction.class, CREATE, same(connection), eq(absolutePath));

        ecs.addExportToBucket(BUCKET_NAME, NAMESPACE_NAME, EXPORT_NAME_VALUE);

        ArgumentCaptor<String> listPathCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NFSExportAction.class);
        NFSExportAction.list(same(connection), listPathCaptor.capture());
        assertEquals(absolutePath, listPathCaptor.getValue());

        ArgumentCaptor<String> createPathCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NFSExportAction.class);
        NFSExportAction.create(same(connection), createPathCaptor.capture());
        assertEquals(absolutePath, createPathCaptor.getValue());
    }

    /**
     * A service can add an export to a bucket
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testAddNullExportPathToBucket() throws Exception {
        String absolutePath = "/" + NAMESPACE_NAME + "/" + PREFIX + BUCKET_NAME + "/";
        PowerMockito.mockStatic(NFSExportAction.class);

        when(NFSExportAction.list(same(connection), eq(absolutePath)))
                .thenReturn(null);

        PowerMockito.doNothing().when(NFSExportAction.class, CREATE, same(connection), eq(absolutePath));

        ecs.addExportToBucket(BUCKET_NAME, NAMESPACE_NAME, null);

        ArgumentCaptor<String> listPathCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NFSExportAction.class);
        NFSExportAction.list(same(connection), listPathCaptor.capture());
        assertEquals(absolutePath, listPathCaptor.getValue());

        ArgumentCaptor<String> createPathCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NFSExportAction.class);
        NFSExportAction.create(same(connection), createPathCaptor.capture());
        assertEquals(absolutePath, createPathCaptor.getValue());
    }

    /**
     * A service can change tags of the existing bucket.
     * Provided parameters include list of tags that could be added for the first time or overwritten.
     *
     * @throws Exception on mocking called classes
     */
    @Test
    public void changeBucketTagsDefaultTest() throws Exception {
        setupChangeBucketTagsTest(createListOfTags(KEY1, VALUE1, KEY2, VALUE2));

        Map<String, Object> params = new HashMap<>();
        params.put(TAGS, createListOfTags(KEY1, VALUE2, KEY3, VALUE3));
        Map<String, Object> serviceSettings = ecs.changeBucketTags(BUCKET_NAME, NAMESPACE_NAME, params);
        List<Map<String, String>> setTags = (List<Map<String, String>>) serviceSettings.get(TAGS);

        List<Map<String, String>> expectedTags = createListOfTags(KEY1, VALUE2, KEY2, VALUE2, KEY3, VALUE3);

        assertTrue(CollectionUtils.isEqualCollection(expectedTags, setTags));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);

        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.get(same(connection), idCaptor.capture(), nsCaptor.capture());

        assertEquals(NAMESPACE_NAME, nsCaptor.getValue());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());

        ArgumentCaptor<BucketTagsParamAdd> tagsParamAddCaptor = ArgumentCaptor.forClass(BucketTagsParamAdd.class);

        PowerMockito.verifyStatic(BucketTagsAction.class, times(1));
        BucketTagsAction.create(same(connection), idCaptor.capture(), tagsParamAddCaptor.capture());
        BucketTagsParamAdd tagsParamAdd = tagsParamAddCaptor.getValue();
        List<Map<String, String>> expectedCreatedTags = createListOfTags(KEY3, VALUE3);
        List<Map<String, String>> invokedCreatedTags = tagsParamAdd.getTagSetAsListOfTags();
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertTrue(CollectionUtils.isEqualCollection(expectedCreatedTags, invokedCreatedTags));
        assertEquals(NAMESPACE_NAME, tagsParamAdd.getNamespace());

        ArgumentCaptor<BucketTagsParamUpdate> tagsParamUpdateCaptor = ArgumentCaptor.forClass(BucketTagsParamUpdate.class);

        PowerMockito.verifyStatic(BucketTagsAction.class, times(1));
        BucketTagsAction.update(same(connection), idCaptor.capture(), tagsParamUpdateCaptor.capture());
        BucketTagsParamUpdate tagsParamUpdate = tagsParamUpdateCaptor.getValue();
        List<Map<String, String>> expectedUpdatedTags = createListOfTags(KEY1, VALUE2);
        List<Map<String, String>> invokedUpdatedTags = tagsParamUpdate.getTagSetAsListOfTags();
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertTrue(CollectionUtils.isEqualCollection(expectedUpdatedTags, invokedUpdatedTags));
        assertEquals(NAMESPACE_NAME, tagsParamAdd.getNamespace());
    }

    /**
     * A service can overwrite tags of the existing bucket.
     *
     * @throws Exception on mocking called classes
     */
    @Test
    public void changeBucketTagsUpdateTest() throws Exception {
        setupChangeBucketTagsTest(createListOfTags(KEY1, VALUE1));

        Map<String, Object> params = new HashMap<>();
        params.put(TAGS, createListOfTags(KEY1, VALUE2));
        Map<String, Object> serviceSettings = ecs.changeBucketTags(BUCKET_NAME, NAMESPACE_NAME, params);
        List<Map<String, String>> setTags = (List<Map<String, String>>) serviceSettings.get(TAGS);

        List<Map<String, String>> expectedTags = createListOfTags(KEY1, VALUE2);

        assertTrue(CollectionUtils.isEqualCollection(expectedTags, setTags));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BucketTagsParamUpdate> tagsParamCaptor = ArgumentCaptor.forClass(BucketTagsParamUpdate.class);

        PowerMockito.verifyStatic(BucketTagsAction.class, times(1));
        BucketTagsAction.update(same(connection), idCaptor.capture(), tagsParamCaptor.capture());
        BucketTagsParamUpdate tagsParam = tagsParamCaptor.getValue();
        List<Map<String, String>> expectedCreatedTags = createListOfTags(KEY1, VALUE2);
        List<Map<String, String>> invokedCreatedTags = tagsParam.getTagSetAsListOfTags();
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertTrue(CollectionUtils.isEqualCollection(expectedCreatedTags, invokedCreatedTags));
        assertEquals(NAMESPACE_NAME, tagsParam.getNamespace());
    }

    /**
     * A service would not overwrite tags of the existing bucket if their values same.
     *
     * @throws Exception on mocking called classes
     */
    @Test
    public void changeBucketTagsNoUpdateTest() throws Exception {
        setupChangeBucketTagsTest(createListOfTags(KEY1, VALUE1));

        Map<String, Object> params = new HashMap<>();
        params.put(TAGS, createListOfTags(KEY1, VALUE1));
        Map<String, Object> serviceSettings = ecs.changeBucketTags(BUCKET_NAME, NAMESPACE_NAME, params);
        List<Map<String, String>> setTags = (List<Map<String, String>>) serviceSettings.get(TAGS);

        List<Map<String, String>> expectedTags =
                createListOfTags(KEY1, VALUE1);

        assertTrue(CollectionUtils.isEqualCollection(expectedTags, setTags));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BucketTagsParamUpdate> tagsParamCaptor = ArgumentCaptor.forClass(BucketTagsParamUpdate.class);

        PowerMockito.verifyStatic(BucketTagsAction.class, times(0));
        BucketTagsAction.update(same(connection), idCaptor.capture(), tagsParamCaptor.capture());
    }

    /**
     * A service can create new tags of the existing bucket.
     *
     * @throws Exception on mocking called classes
     */
    @Test
    public void changeBucketTagsCreateTest() throws Exception {
        setupChangeBucketTagsTest(createListOfTags(KEY1, VALUE1));

        Map<String, Object> params = new HashMap<>();
        params.put(TAGS, createListOfTags(KEY2, VALUE2));

        Map<String, Object> serviceSettings = ecs.changeBucketTags(BUCKET_NAME, NAMESPACE_NAME, params);

        List<Map<String, String>> setTags = (List<Map<String, String>>) serviceSettings.get(TAGS);
        List<Map<String, String>> expectedTags = createListOfTags(KEY1, VALUE1, KEY2, VALUE2);

        assertTrue(CollectionUtils.isEqualCollection(expectedTags, setTags));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BucketTagsParamAdd> tagsParamCaptor = ArgumentCaptor.forClass(BucketTagsParamAdd.class);

        PowerMockito.verifyStatic(BucketTagsAction.class, times(1));
        BucketTagsAction.create(same(connection), idCaptor.capture(), tagsParamCaptor.capture());
        BucketTagsParamAdd tagsParam = tagsParamCaptor.getValue();
        List<Map<String, String>> expectedCreatedTags = createListOfTags(KEY2, VALUE2);
        List<Map<String, String>> invokedCreatedTags = tagsParam.getTagSetAsListOfTags();
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertTrue(CollectionUtils.isEqualCollection(expectedCreatedTags, invokedCreatedTags));
        assertEquals(NAMESPACE_NAME, tagsParam.getNamespace());
    }

    @Test
    public void validateMetadataSearchInvalidDatatypeTest() {
        assertThrows(ServiceBrokerInvalidParametersException.class, () -> {
            Map<String, Object> parameters = new HashMap<>();
            List<Map<String, String>> searchMetadata = createListOfSearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SYSTEM_METADATA_NAME, INVALID_METADATA_TYPE);
            parameters.put(SEARCH_METADATA, searchMetadata);
            ecs.validateAndPrepareSearchMetadata(parameters);
        });
    }

    @Test
    public void validateMetadataSearchNoNameTest() {
        assertThrows(ServiceBrokerInvalidParametersException.class, () -> {
            Map<String, Object> parameters = new HashMap<>();
            List<Map<String, String>> searchMetadata = createListOfSearchMetadata(SEARCH_METADATA_TYPE_USER, null, USER_METADATA_TYPE);
            parameters.put(SEARCH_METADATA, searchMetadata);
            ecs.validateAndPrepareSearchMetadata(parameters);
        });
    }

    @Test
    public void validateMetadataSearchInvalidSystemMetadataNameTest() {
        assertThrows(ServiceBrokerInvalidParametersException.class, () -> {
            Map<String, Object> parameters = new HashMap<>();
            List<Map<String, String>> searchMetadata = createListOfSearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, USER_METADATA_NAME, USER_METADATA_TYPE);
            parameters.put(SEARCH_METADATA, searchMetadata);
            ecs.validateAndPrepareSearchMetadata(parameters);
        });
    }

    @Test
    public void validateMetadataSearchInvalidSystemMetadataDatatypeTest() {
        assertThrows(ServiceBrokerInvalidParametersException.class, () -> {
            Map<String, Object> parameters = new HashMap<>();
            List<Map<String, String>> searchMetadata = createListOfSearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SYSTEM_METADATA_NAME, USER_METADATA_TYPE);
            parameters.put(SEARCH_METADATA, searchMetadata);
            ecs.validateAndPrepareSearchMetadata(parameters);
        });
    }

    @Test
    public void validateMetadataSearchUserMetadataPrefixAdditionTest() {
        Map<String, Object> parameters = new HashMap<>();
        List<Map<String, String>> searchMetadata = createListOfSearchMetadata(SEARCH_METADATA_TYPE_USER, USER_METADATA_NAME, USER_METADATA_TYPE);
        parameters.put(SEARCH_METADATA, searchMetadata);
        Map<String, Object> validatedParameters = ecs.validateAndPrepareSearchMetadata(parameters);
        List<Map<String, String>> validatedSearchMetadata = (List<Map<String, String>>) validatedParameters.get(SEARCH_METADATA);
        String name = validatedSearchMetadata.get(0).get(SEARCH_METADATA_NAME);
        assertTrue(name.startsWith(SEARCH_METADATA_USER_PREFIX));
    }

    private void setupInitTest() throws EcsManagementClientException {
        DataServiceReplicationGroup rg = new DataServiceReplicationGroup();
        rg.setName(RG_NAME);
        rg.setId(RG_ID);

        UserSecretKey secretKey = new UserSecretKey();
        secretKey.setSecretKey(TEST);

        PowerMockito.mockStatic(BucketAction.class);
        when(BucketAction.exists(connection, REPO_BUCKET, NAMESPACE_NAME))
                .thenReturn(true);

        PowerMockito.mockStatic(ReplicationGroupAction.class);
        when(ReplicationGroupAction.list(connection))
                .thenReturn(Collections.singletonList(rg));

        PowerMockito.mockStatic(ObjectUserAction.class);
        when(ObjectUserAction.exists(connection, REPO_USER, NAMESPACE_NAME))
                .thenReturn(true);

        PowerMockito.mockStatic(ObjectUserSecretAction.class);
        when(ObjectUserSecretAction.list(connection, REPO_USER))
                .thenReturn(Collections.singletonList(secretKey));
    }

    private void setupBaseUrlTest(String name, boolean namespaceInHost) throws EcsManagementClientException {
        PowerMockito.mockStatic(BaseUrlAction.class);
        BaseUrl baseUrl = new BaseUrl();
        baseUrl.setId(BASE_URL_ID);
        baseUrl.setName(name);
        when(BaseUrlAction.list(same(connection)))
                .thenReturn(Collections.singletonList(baseUrl));

        BaseUrlInfo baseUrlInfo = new BaseUrlInfo();
        baseUrlInfo.setId(BASE_URL_ID);
        baseUrlInfo.setName(name);
        baseUrlInfo.setNamespaceInHost(namespaceInHost);
        baseUrlInfo.setBaseurl(BASE_URL);
        when(BaseUrlAction.get(connection, BASE_URL_ID))
                .thenReturn(baseUrlInfo);
    }

    private void setupCreateBucketQuotaTest(int limit, int warn)
            throws Exception {
        PowerMockito.mockStatic(BucketQuotaAction.class);
        PowerMockito.doNothing().when(BucketQuotaAction.class, CREATE,
                same(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE_NAME),
                eq(limit), eq(warn));
    }

    private void setupBucketRetentionUpdate(int expectedValue) throws Exception {
        PowerMockito.mockStatic(BucketRetentionAction.class);
        PowerMockito.doNothing().when(BucketRetentionAction.class, UPDATE,
                same(connection), eq(NAMESPACE_NAME), eq(PREFIX + BUCKET_NAME),
                eq(expectedValue));
    }

    private void setupCreateBucketTest() throws Exception {
        setupReplicationGroupsList();

        PowerMockito.mockStatic(BucketAction.class);
        PowerMockito.doNothing().when(BucketAction.class, CREATE,
                same(connection), any(ObjectBucketCreate.class));

        PowerMockito.mockStatic(BucketRetentionAction.class);
    }

    private void setupBucketExistsTest() throws Exception {
        PowerMockito.mockStatic(NamespaceAction.class);
        PowerMockito.when(NamespaceAction.class, EXISTS, same(connection), eq(NAMESPACE_NAME)).thenReturn(true);
        PowerMockito.when(BucketAction.class, EXISTS, same(connection), eq(PREFIX + BUCKET_NAME), anyString()).thenReturn(true);
    }

    private void setupBucketGetTest() throws Exception {
        ObjectBucketInfo bucketInfo = new ObjectBucketInfo();
        bucketInfo.setFsAccessEnabled(true);

        PowerMockito.when(BucketAction.class, GET, same(connection), eq(PREFIX + BUCKET_NAME), anyString())
                .thenReturn(bucketInfo);
    }

    private void setupBucketDeleteTest() throws Exception {
        PowerMockito.doNothing().when(BucketAction.class, DELETE, same(connection), eq(PREFIX + BUCKET_NAME), anyString());
    }

    private void setupBucketAclTest() throws Exception {
        BucketAclAcl bucketAclAcl = new BucketAclAcl();
        bucketAclAcl.setUserAccessList(new ArrayList<>());
        bucketAclAcl.setGroupAccessList(new ArrayList<>());

        BucketAcl bucketAcl = new BucketAcl();
        bucketAcl.setAcl(bucketAclAcl);

        PowerMockito.mockStatic(BucketAclAction.class);
        PowerMockito.when(BucketAclAction.class, GET, same(connection), eq(PREFIX + BUCKET_NAME), anyString()).thenReturn(bucketAcl);
        PowerMockito.doNothing().when(BucketAclAction.class, UPDATE, same(connection), eq(PREFIX + BUCKET_NAME), any());
    }

    private void setupDeleteBucketQuotaTest() throws Exception {
        PowerMockito.mockStatic(BucketQuotaAction.class);
        PowerMockito.doNothing().when(BucketQuotaAction.class, DELETE,
                same(connection), eq(BUCKET_NAME), eq(NAMESPACE_NAME));
    }

    private void setupUpdateNamespaceTest() throws Exception {
        setupReplicationGroupsList();

        PowerMockito.mockStatic(NamespaceAction.class);
        PowerMockito.doNothing().when(NamespaceAction.class, UPDATE,
                same(connection), anyString(), any(NamespaceUpdate.class));
    }

    private void setupCreateNamespaceQuotaTest() throws Exception {
        PowerMockito.mockStatic(NamespaceQuotaAction.class);
        PowerMockito.doNothing().when(NamespaceQuotaAction.class, CREATE,
                same(connection), anyString(), any(NamespaceQuotaParam.class));
    }

    private void setupCreateNamespaceTest() throws Exception {
        setupReplicationGroupsList();

        PowerMockito.mockStatic(NamespaceAction.class);
        PowerMockito.doNothing().when(NamespaceAction.class, CREATE,
                same(connection), any(NamespaceCreate.class));
    }

    private void setupDeleteNamespaceTest() throws Exception {
        PowerMockito.mockStatic(NamespaceAction.class);
        PowerMockito.doNothing().when(NamespaceAction.class, DELETE,
                same(connection), anyString());
        PowerMockito.when(NamespaceAction.class, "exists", same(connection), any(String.class))
                .thenReturn(true);
    }

    private void setupCreateNamespaceRetentionTest(boolean exists) throws Exception {
        PowerMockito.mockStatic(NamespaceRetentionAction.class);
        PowerMockito.when(NamespaceRetentionAction.class, EXISTS, same(connection),
                anyString(), anyString())
                .thenReturn(exists);
        PowerMockito.doNothing().when(NamespaceRetentionAction.class, CREATE,
                same(connection), anyString(), any(RetentionClassCreate.class));
        PowerMockito.doNothing().when(NamespaceRetentionAction.class, UPDATE,
                same(connection), anyString(), anyString(),
                any(RetentionClassUpdate.class));
        PowerMockito.doNothing().when(NamespaceRetentionAction.class, DELETE,
                same(connection), anyString(), anyString());
    }

    private void setupCreateBucketRetentionTest(int retentionPeriod) throws Exception {
        DefaultBucketRetention retention = new DefaultBucketRetention();
        retention.setPeriod(retentionPeriod);
        PowerMockito.mockStatic(BucketRetentionAction.class);
        PowerMockito.doNothing().when(BucketRetentionAction.class, UPDATE,
                same(connection), anyString(), anyString(), anyInt());
        PowerMockito.when(BucketRetentionAction.class, GET,
                same(connection), anyString(), anyString())
                .thenReturn(retention);
    }


    private void setupReplicationGroupsList() throws Exception {
        DataServiceReplicationGroup rg1 = new DataServiceReplicationGroup();
        rg1.setName(RG_NAME);
        rg1.setId(RG_ID);

        DataServiceReplicationGroup rg2 = new DataServiceReplicationGroup();
        rg2.setName(RG_NAME_2);
        rg2.setId(RG_ID_2);

        DataServiceReplicationGroup rg3 = new DataServiceReplicationGroup();
        rg3.setName(RG_NAME_3);
        rg3.setId(RG_ID_3);

        DataServiceReplicationGroup rg4 = new DataServiceReplicationGroup();
        rg4.setName(RG_NAME_4);
        rg4.setId(RG_ID_4);

        List<DataServiceReplicationGroup> replicationGroupsList = Arrays.asList(rg1, rg2, rg3, rg4);

        PowerMockito.mockStatic(ReplicationGroupAction.class);
        PowerMockito.when(ReplicationGroupAction.class, "list", same(connection))
                .thenReturn(replicationGroupsList);
    }

    private void setupChangeBucketTagsTest(List<Map<String, String>> tags) throws Exception {
        if (tags != null) {
            PowerMockito.mockStatic(BucketAction.class);
            ObjectBucketInfo bucket = new ObjectBucketInfo();
            bucket.setTagSetAsListOfMaps(tags);
            PowerMockito.when(BucketAction.class, GET, same(connection), anyString(), anyString()).thenReturn(bucket);
        }
        PowerMockito.mockStatic(BucketTagsAction.class);
        PowerMockito.doNothing().when(BucketTagsAction.class, CREATE, same(connection), anyString(), any(BucketTagsParamAdd.class));
        PowerMockito.doNothing().when(BucketTagsAction.class, UPDATE, same(connection), anyString(), any(BucketTagsParamUpdate.class));
    }

    private List<Map<String, String>> createListOfTags(String... args) throws IllegalArgumentException {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments should be multiple of two.");
        }
        List<Map<String, String>> tags = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2) {
            Map<String, String> tag = new HashMap<>();
            tag.put(BucketTagSetRootElement.KEY, args[i]);
            tag.put(BucketTagSetRootElement.VALUE, args[i + 1]);
            tags.add(tag);
        }
        return tags;
    }

    private List<Map<String, String>> createListOfSearchMetadata(String... args) throws IllegalArgumentException {
        if (args.length % 3 != 0) {
            throw new IllegalArgumentException("Number of arguments should be multiple of three.");
        }
        List<Map<String, String>> searchMetadata = new ArrayList<>();
        for (int i = 0; i < args.length; i += 3) {
            Map<String, String> metadata = new HashMap<>();
            if(args[i] != null)
                metadata.put(SEARCH_METADATA_TYPE, args[i]);
            if(args[i + 1] != null)
                metadata.put(SEARCH_METADATA_NAME, args[i + 1]);
            if(args[i + 2] != null)
                metadata.put(SEARCH_METADATA_DATATYPE, args[i + 2]);
            searchMetadata.add(metadata);
        }
        return searchMetadata;
    }
}
