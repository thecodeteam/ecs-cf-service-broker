package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.*;
import com.emc.ecs.management.sdk.model.*;
import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReplicationGroupAction.class, BucketAction.class,
        ObjectUserAction.class, ObjectUserSecretAction.class,
        BaseUrlAction.class, BucketQuotaAction.class,
        BucketRetentionAction.class, NamespaceAction.class,
        NamespaceQuotaAction.class, NamespaceRetentionAction.class,
        BucketAclAction.class, NFSExportAction.class, ObjectUserMapAction.class})
public class EcsServiceTest {
    private static final String FOO = "foo";
    private static final String ONE_YEAR = "one-year";
    private static final int ONE_YEAR_IN_SECS = 31536000;
    private static final String BASE_URL = "base-url";
    private static final String USE_SSL = "use-ssl";
    private static final String USER1 = "user1";
    private static final String EXISTS = "exists";
    private static final String DEFAULT_BUCKET_QUOTA = "default-bucket-quota";
    private static final String DOMAIN_GROUP_ADMINS = "domain-group-admins";
    private static final String ACCESS_DURING_OUTAGE = "access-during-outage";
    private static final String ENCRYPTED = "encrypted";
    private static final String REPOSITORY = "repository";
    private static final String USER = "user";
    private static final String WARN = "warn";
    private static final String QUOTA = "quota";
    private static final String LIMIT = "limit";
    private static final String DOT = ".";
    private static final String HTTPS = "https://";
    private static final int THIRTY_DAYS_IN_SEC = 2592000;
    private static final String HTTP = "http://";
    private static final String _9020 = ":9020";
    private static final String _9021 = ":9021";
    private static final String RETENTION = "retention";
    private static final String THIRTY_DAYS = "thirty-days";
    private static final String UPDATE = "update";
    private static final String CREATE = "create";
    private static final String DELETE = "delete";
    public static final String COMPLIANCE_ENABLED = "compliance-enabled";
    public static final String DEFAULT_RETENTION = "default-retention";

    @Mock
    private Connection connection;

    @Mock
    private BrokerConfig broker;

    @Mock
    private CatalogConfig catalog;

    @Autowired
    @InjectMocks
    private EcsService ecs;

    @Before
    public void setUp() {
        when(broker.getPrefix()).thenReturn(PREFIX);
        when(broker.getReplicationGroup()).thenReturn(RG_NAME);
        when(broker.getNamespace()).thenReturn(NAMESPACE);
        when(broker.getRepositoryUser()).thenReturn(USER);
        when(broker.getRepositoryBucket()).thenReturn(REPOSITORY);
    }

    /**
     * When initializing the ecs-service, and object-endpoint, repo-user &
     * repo-bucket are set, the service will use these static settings. It the
     * repo facilities exist, the ecs-service will continue.
     *
     * @throws EcsManagementClientException
     * @throws EcsManagementResourceNotFoundException
     */
    @Test
    public void initializeStaticConfigTest() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
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
     * @throws EcsManagementClientException
     * @throws EcsManagementResourceNotFoundException
     */
    @Test
    public void initializeBaseUrlLookup() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        setupInitTest();
        setupBaseUrlTest(BASE_URL_NAME, false);
        when(broker.getBaseUrl()).thenReturn(BASE_URL_NAME);

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
     * @throws EcsManagementClientException
     * @throws EcsManagementResourceNotFoundException
     */
    @Test
    public void initializeBaseUrlDefaultLookup()
            throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
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
     * @throws EcsManagementClientException
     * @throws EcsManagementResourceNotFoundException
     */
    @Test(expected = ServiceBrokerException.class)
    public void initializeBaseUrlDefaultLookupFails()
            throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        PowerMockito.mockStatic(BaseUrlAction.class);
        when(BaseUrlAction.list(same(connection)))
                .thenReturn(Collections.emptyList());

        ecs.initialize();
    }

    /**
     * When creating a new bucket the settings in the plan will carry through to
     * the created service. Any settings not implemented in the service, the
     * plan or the parameters will be kept as null.
     *
     * The create command will return a map including the resolved service
     * settings.
     *
     * @throws Exception
     */
    @Test
    public void createBucketDefaultTest() throws Exception {
        setupCreateBucketTest();
        setupCreateBucketQuotaTest(5, 4);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, service, plan, params);

        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get("quota");
        assertEquals(4, quota.get(WARN).longValue());
        assertEquals(5, quota.get(LIMIT).longValue());

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor
                .forClass(ObjectBucketCreate.class);
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());
        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + BUCKET_NAME, create.getName());
        assertNull(create.getIsEncryptionEnabled());
        assertNull(create.getIsStaleAllowed());
        assertEquals(NAMESPACE, create.getNamespace());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), eq(PREFIX + BUCKET_NAME),
                eq(NAMESPACE), eq(5), eq(4));
    }

    /**
     * When creating a bucket plan with no user specified parameters, the plan
     * or service settings will be used. The plan service-settings will be
     * observed.
     *
     * @throws Exception
     */
    @Test
    public void createBucketWithoutParamsTest() throws Exception {
        setupCreateBucketTest();
        PowerMockito.mockStatic(BucketQuotaAction.class);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID2);
        when(catalog.findServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(service);

        Map<String, Object> serviceSettings =
                ecs.createBucket(BUCKET_NAME, service, plan, new HashMap<>());
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertTrue((Boolean) serviceSettings.get(FILE_ACCESSIBLE));
        assertNull(serviceSettings.get(QUOTA));

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor
                .forClass(ObjectBucketCreate.class);
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());

        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + BUCKET_NAME, create.getName());
        assertEquals(NAMESPACE, create.getNamespace());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertTrue(create.getFilesystemEnabled());
        assertEquals("s3", create.getHeadType());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(0));
        BucketQuotaAction.create(any(Connection.class), anyString(),
                anyString(), anyInt(), anyInt());
    }

    public void createBucketWithParamsTest() throws Exception {
        setupCreateBucketTest();
        setupCreateBucketQuotaTest(5, 4);

        Map<String, Object> params = new HashMap<>();
        params.put(ENCRYPTED, true);
        params.put(ACCESS_DURING_OUTAGE, true);
        Map<String, Object> quota = new HashMap<>();
        quota.put(WARN, 9);
        quota.put(LIMIT, 10);
        params.put(QUOTA, quota);
        params.put(FILE_ACCESSIBLE, true);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, service, plan, params);
        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, returnQuota.get(WARN).longValue());
        assertEquals(5, returnQuota.get(LIMIT).longValue());
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertTrue((Boolean) serviceSettings.get(FILE_ACCESSIBLE));

        ArgumentCaptor<ObjectBucketCreate> createCaptor = ArgumentCaptor
                .forClass(ObjectBucketCreate.class);
        PowerMockito.verifyStatic(BucketAction.class, times(1));
        BucketAction.create(same(connection), createCaptor.capture());

        ObjectBucketCreate create = createCaptor.getValue();
        assertEquals(PREFIX + BUCKET_NAME, create.getName());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertTrue(create.getFilesystemEnabled());
        assertEquals(NAMESPACE, create.getNamespace());

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), eq(PREFIX + BUCKET_NAME),
                eq(NAMESPACE), eq(5), eq(4));
    }

    /**
     * Buckets are not file enabled by default
     *
     * @throws Exception
     */
    @Test
    public void getBucketFileEnabledTest() throws Exception {
        ObjectBucketInfo fakeBucket = new ObjectBucketInfo();

        PowerMockito.mockStatic(BucketAction.class);
        PowerMockito.when(BucketAction.class, GET,
                same(connection), anyString(), anyString()).thenReturn(fakeBucket);

        boolean isEnabled = ecs.getBucketFileEnabled(FOO);
        assertEquals(false, isEnabled);

        fakeBucket.setFsAccessEnabled(true);
        isEnabled = ecs.getBucketFileEnabled(FOO);
        assertEquals(true, isEnabled);
    }


    /**
     * When changing plans from one with a quota to one without a quota any
     * existing quota should be deleted.
     *
     * @throws Exception
     */
    @Test
    public void changeBucketPlanTestNoQuota() throws Exception {
        setupDeleteBucketQuotaTest();
        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID2);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, new HashMap<>());
        assertNull(serviceSettings.get(QUOTA));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.delete(same(connection), idCaptor.capture(),
                nsCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE, nsCaptor.getValue());
    }

    /**
     * When changing plans from one without a quota and quota parameters are
     * supplied, the quota parameters must dictate the quota created.
     *
     * @throws Exception
     */
    @Test
    public void changeBucketPlanTestParametersQuota() throws Exception {
        setupCreateBucketQuotaTest(100, 80);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID2);

        Map<String, Object> quota = new HashMap<>();
        quota.put(LIMIT, 100);
        quota.put(WARN, 80);
        Map<String, Object> params = new HashMap<>();
        params.put(QUOTA, quota);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, params);
        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(80, returnQuota.get(WARN).longValue());
        assertEquals(100, returnQuota.get(LIMIT).longValue());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor
                .forClass(Integer.class);
        ArgumentCaptor<Integer> warnCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), idCaptor.capture(),
                nsCaptor.capture(), limitCaptor.capture(),
                warnCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE, nsCaptor.getValue());
        assertEquals(Integer.valueOf(100), limitCaptor.getValue());
        assertEquals(Integer.valueOf(80), warnCaptor.getValue());
    }

    /**
     * When changing plans from one with a quota and quota parameters are
     * supplied, the quota parameters must be ignored.
     *
     * @throws Exception
     */
    @Test
    public void changeBucketPlanTestParametersIgnoredQuota() throws Exception {
        setupCreateBucketQuotaTest(5, 4);
        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> quota = new HashMap<>();
        quota.put(LIMIT, 100);
        quota.put(WARN, 80);
        Map<String, Object> params = new HashMap<>();
        params.put(QUOTA, quota);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, params);
        Map<String, Integer> returnQuota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, returnQuota.get(WARN).longValue());
        assertEquals(5, returnQuota.get(LIMIT).longValue());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor
                .forClass(Integer.class);
        ArgumentCaptor<Integer> warnCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), idCaptor.capture(),
                nsCaptor.capture(), limitCaptor.capture(),
                warnCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE, nsCaptor.getValue());
        assertEquals(Integer.valueOf(5), limitCaptor.getValue());
        assertEquals(Integer.valueOf(4), warnCaptor.getValue());
    }

    /**
     * When changing plans from one without a quota to one with a quota the new
     * quota should be created.
     *
     * @throws Exception
     */
    @Test
    public void changeBucketPlanTestNewQuota() throws Exception {
        setupCreateBucketQuotaTest(5, 4);

        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> serviceSettings = ecs.changeBucketPlan(BUCKET_NAME, service, plan, new HashMap<>());
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(WARN).longValue());
        assertEquals(5, quota.get(LIMIT).longValue());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor
                .forClass(Integer.class);
        ArgumentCaptor<Integer> warnCaptor = ArgumentCaptor
                .forClass(Integer.class);

        PowerMockito.verifyStatic(BucketQuotaAction.class, times(1));
        BucketQuotaAction.create(same(connection), idCaptor.capture(),
                nsCaptor.capture(), limitCaptor.capture(),
                warnCaptor.capture());
        assertEquals(PREFIX + BUCKET_NAME, idCaptor.getValue());
        assertEquals(NAMESPACE, nsCaptor.getValue());
        assertEquals(Integer.valueOf(5), limitCaptor.getValue());
        assertEquals(Integer.valueOf(4), warnCaptor.getValue());
    }

    /**
     * A service must be able to remove a user from a bucket.
     *
     * @throws Exception
     */
    @Test
    public void removeUserFromBucketTest() throws Exception {
        BucketAcl bucketAcl = new BucketAcl();
        BucketUserAcl userAcl = new BucketUserAcl(PREFIX + USER1,
                Collections.singletonList("full_control"));
        BucketAclAcl acl = new BucketAclAcl();
        acl.setUserAccessList(Collections.singletonList(userAcl));
        bucketAcl.setAcl(acl);
        PowerMockito.mockStatic(BucketAclAction.class);
        PowerMockito
                .when(BucketAclAction.class, GET, same(connection),
                        eq(PREFIX + BUCKET_NAME), eq(NAMESPACE))
                .thenReturn(bucketAcl);
        PowerMockito.doNothing().when(BucketAclAction.class, UPDATE,
                same(connection), eq(PREFIX + BUCKET_NAME),
                any(BucketAcl.class));

        ecs.removeUserFromBucket(BUCKET_NAME, USER1);

        PowerMockito.verifyStatic(BucketAclAction.class);
        BucketAclAction.get(eq(connection), eq(PREFIX + BUCKET_NAME),
                eq(NAMESPACE));
        ArgumentCaptor<BucketAcl> aclCaptor = ArgumentCaptor
                .forClass(BucketAcl.class);
        PowerMockito.verifyStatic(BucketAclAction.class);
        BucketAclAction.update(eq(connection), eq(PREFIX + BUCKET_NAME),
                aclCaptor.capture());
        List<BucketUserAcl> actualUserAcl = aclCaptor.getValue().getAcl()
                .getUserAccessList();
        assertFalse(actualUserAcl.contains(userAcl));
    }

    /**
     * A service must be able to delete a user.
     *
     * @throws EcsManagementClientException
     */
    public void deleteUser() throws EcsManagementClientException {
        PowerMockito.mockStatic(ObjectUserAction.class);
        ecs.deleteUser(USER1);
        PowerMockito.verifyStatic(ObjectUserAction.class);
        ObjectUserAction.delete(same(connection), eq(PREFIX + USER1));
    }

    /**
     * When creating a new namespace the settings in the plan will carry through
     * to the created service. Any settings not implemented in the service, the
     * plan or the parameters will be kept as null.
     *
     * @throws Exception
     */
    @Test
    public void createNamespaceDefaultTest() throws Exception {
        setupCreateNamespaceTest();
        setupCreateNamespaceQuotaTest();

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(0);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE, namespaceServiceFixture(), plan, params);
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(WARN).longValue());
        assertEquals(5, quota.get(LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);

        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor
                .forClass(NamespaceCreate.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE, create.getNamespace());
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
        assertEquals(PREFIX + NAMESPACE, idCaptor.getValue());
        assertEquals(5, quotaParamCaptor.getValue().getBlockSize());
        assertEquals(4, quotaParamCaptor.getValue().getNotificationSize());
    }

    /**
     * When changing plans the params of the new plan should be set, on the
     * existing namespace. The resulting update action will have the settings of
     * the new plan.
     *
     * @throws Exception
     */
    @Test
    public void changeNamespacePlanTest() throws Exception {
        setupUpdateNamespaceTest();

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceUpdate> updateCaptor = ArgumentCaptor
                .forClass(NamespaceUpdate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID2);
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE, service, plan, params);
        assertNull(serviceSettings.get(QUOTA));

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.update(same(connection), idCaptor.capture(),
                updateCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, idCaptor.getValue());
        NamespaceUpdate update = updateCaptor.getValue();
        assertEquals(EXTERNAL_ADMIN, update.getExternalGroupAdmins());
        assertTrue(update.getIsEncryptionEnabled());
        assertTrue(update.getIsComplianceEnabled());
        assertTrue(update.getIsStaleAllowed());
    }

    /**
     * When creating a plan with no user specified parameters, the plan or
     * service settings will be used. The default-bucket-quota will be "5".
     *
     * @throws Exception
     */
    @Test
    public void createNamespaceWithoutParamsTest() throws Exception {
        setupCreateNamespaceTest();
        setupCreateNamespaceQuotaTest();

        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor
                .forClass(NamespaceCreate.class);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(0);
        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(service);

        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE, service, plan, new HashMap<>());
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertEquals(4, quota.get(WARN).longValue());
        assertEquals(5, quota.get(LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE, create.getNamespace());
        assertEquals(null, create.getExternalGroupAdmins());
        assertEquals(null, create.getIsEncryptionEnabled());
        assertEquals(null, create.getIsComplianceEnabled());
        assertEquals(null, create.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), create.getDefaultBucketBlockSize());

        PowerMockito.verifyStatic(NamespaceQuotaAction.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceQuotaParam> quotaParamCaptor = ArgumentCaptor
                .forClass(NamespaceQuotaParam.class);
        NamespaceQuotaAction.create(same(connection), idCaptor.capture(),
                quotaParamCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, idCaptor.getValue());
        assertEquals(5, quotaParamCaptor.getValue().getBlockSize());
        assertEquals(4, quotaParamCaptor.getValue().getNotificationSize());
    }

    /**
     * When creating plan with user specified parameters, the params should be
     * set, except when overridden by a plan or service settings. Therefore,
     * default-bucket-quota will not be "10", it will be "5" since that's the
     * setting in the plan. Other parameter settings will carry through.
     *
     * @throws Exception
     */
    @Test
    public void createNamespaceWithParamsTest() throws Exception {
        setupCreateNamespaceTest();

        Map<String, Object> params = new HashMap<>();
        params.put(DOMAIN_GROUP_ADMINS, EXTERNAL_ADMIN);
        params.put(ENCRYPTED, true);
        params.put(COMPLIANCE_ENABLED, true);
        params.put(ACCESS_DURING_OUTAGE, true);
        params.put(DEFAULT_BUCKET_QUOTA, 10);

        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor
                .forClass(NamespaceCreate.class);

        setupCreateNamespaceQuotaTest();
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.getPlans().get(0);
        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(service);

        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE, service, plan, params);
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(COMPLIANCE_ENABLED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertEquals(5, serviceSettings.get(DEFAULT_BUCKET_QUOTA));
        assertEquals(4, quota.get(WARN).longValue());
        assertEquals(5, quota.get(LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE, create.getNamespace());
        assertEquals(EXTERNAL_ADMIN, create.getExternalGroupAdmins());
        assertTrue(create.getIsEncryptionEnabled());
        assertTrue(create.getIsComplianceEnabled());
        assertTrue(create.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), create.getDefaultBucketBlockSize());

        PowerMockito.verifyStatic(NamespaceQuotaAction.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NamespaceQuotaParam> quotaParamCaptor = ArgumentCaptor
                .forClass(NamespaceQuotaParam.class);
        NamespaceQuotaAction.create(same(connection), idCaptor.capture(),
                quotaParamCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, idCaptor.getValue());
        assertEquals(5, quotaParamCaptor.getValue().getBlockSize());
        assertEquals(4, quotaParamCaptor.getValue().getNotificationSize());
    }

    /**
     * When changing namespace plan with user specified parameters, the params
     * should be set, except when overridden by a plan or service setting.
     * Therefore, default-bucket-quota will not be "10", it will be "5" since
     * that's the setting in the plan. Other settings will carry through.
     *
     * @throws Exception
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
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE, service, plan, params);
        Map<String, Integer> quota = (Map<String, Integer>) serviceSettings.get(QUOTA);
        assertTrue((Boolean) serviceSettings.get(ENCRYPTED));
        assertTrue((Boolean) serviceSettings.get(ACCESS_DURING_OUTAGE));
        assertTrue((Boolean) serviceSettings.get(COMPLIANCE_ENABLED));
        assertEquals(EXTERNAL_ADMIN, serviceSettings.get(DOMAIN_GROUP_ADMINS));
        assertEquals(5, serviceSettings.get(DEFAULT_BUCKET_QUOTA));
        assertEquals(4, quota.get(WARN).longValue());
        assertEquals(5, quota.get(LIMIT).longValue());

        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.update(same(connection), idCaptor.capture(),
                updateCaptor.capture());
        NamespaceUpdate update = updateCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE, idCaptor.getValue());
        assertEquals(EXTERNAL_ADMIN, update.getExternalGroupAdmins());
        assertTrue(update.getIsEncryptionEnabled());
        assertTrue(update.getIsComplianceEnabled());
        assertTrue(update.getIsStaleAllowed());
        assertEquals(Integer.valueOf(5), update.getDefaultBucketBlockSize());
    }

    /**
     * When creating a namespace with retention-class defined in the plan and
     * the retention-class does not already exist, the retention class should be
     * created.
     *
     * @throws Exception
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

        Map<String, Object> serviceSettings = ecs.createNamespace(NAMESPACE, service, plan, params);
        Map<String, Object> returnRetention = (Map<String, Object>) serviceSettings.get(RETENTION);
        assertEquals(ONE_YEAR_IN_SECS, returnRetention.get(ONE_YEAR));

        PowerMockito.verifyStatic(NamespaceAction.class);
        ArgumentCaptor<NamespaceCreate> createCaptor = ArgumentCaptor
                .forClass(NamespaceCreate.class);
        NamespaceAction.create(same(connection), createCaptor.capture());
        NamespaceCreate create = createCaptor.getValue();
        assertEquals(PREFIX + NAMESPACE, create.getNamespace());
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
        assertEquals(PREFIX + NAMESPACE, idCaptor.getValue());
        assertEquals(ONE_YEAR, retention.getName());
        assertEquals(ONE_YEAR_IN_SECS, retention.getPeriod());
    }

    /**
     * When changing a namespace plan and a different retention-class is
     * specified in the parameters, then the retention class should be added.
     *
     * @throws Exception
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
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE, service, plan, params);
        Map<String, Object> returnRetention = (Map<String, Object>) serviceSettings.get(RETENTION);
        assertEquals(THIRTY_DAYS_IN_SEC, returnRetention.get(THIRTY_DAYS));

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);
        NamespaceRetentionAction.create(same(connection), nsCaptor.capture(),
                createCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, nsCaptor.getValue());
        assertEquals(THIRTY_DAYS, createCaptor.getValue().getName());
        assertEquals(THIRTY_DAYS_IN_SEC, createCaptor.getValue().getPeriod());
    }

    /**
     * When changing a namespace plan and a retention-class is specified in the
     * parameters with the value -1, then the retention class should be removed.
     *
     * @throws Exception
     */
    @Test
    public void changeNamespacePlanRemoveRentention() throws Exception {
        Map<String, Object> retention = new HashMap<>();
        retention.put(THIRTY_DAYS, -1);
        Map<String, Object> params = new HashMap<>();
        params.put(RETENTION, retention);

        setupUpdateNamespaceTest();
        setupCreateNamespaceRetentionTest(true);

        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID2);
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE, service, plan, params);
        assertNull(serviceSettings.get(RETENTION));

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rcCaptor = ArgumentCaptor.forClass(String.class);
        NamespaceRetentionAction.delete(same(connection), nsCaptor.capture(),
                rcCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, nsCaptor.getValue());
        assertEquals(THIRTY_DAYS, rcCaptor.getValue());
    }

    /**
     * When changing a namespace plan and a retention-class is specified in the
     * parameters with a different value than the existing value, then the
     * retention-class should be changed.
     *
     * @throws Exception
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
        Map<String, Object> serviceSettings = ecs.changeNamespacePlan(NAMESPACE, service, plan, params);
        Map<String, Object> returnRetention = (Map<String, Object>) serviceSettings.get(RETENTION);
        assertEquals(THIRTY_DAYS_IN_SEC, returnRetention.get(THIRTY_DAYS));

        PowerMockito.verifyStatic(NamespaceRetentionAction.class);
        NamespaceRetentionAction.update(same(connection), nsCaptor.capture(),
                rcCaptor.capture(), updateCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, nsCaptor.getValue());
        assertEquals(THIRTY_DAYS, rcCaptor.getValue());
        assertEquals(THIRTY_DAYS_IN_SEC, updateCaptor.getValue().getPeriod());
    }

    /**
     * When creating a namespace with retention-class defined in the plan and
     * the retention-class does not already exist, the retention class should be
     * created.
     *
     * @throws Exception
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

        Map<String, Object> serviceSettings = ecs.createBucket(BUCKET_NAME, service, plan, params);
        assertEquals(THIRTY_DAYS_IN_SEC, serviceSettings.get(DEFAULT_RETENTION));

        PowerMockito.verifyStatic(BucketRetentionAction.class);
        BucketRetentionAction.update(same(connection), eq(NAMESPACE),
                eq(PREFIX + BUCKET_NAME), eq(THIRTY_DAYS_IN_SEC));
    }

    /**
     * A namespace should be able to be deleted.
     *
     * @throws Exception
     */
    @PrepareForTest({NamespaceAction.class})
    @Test
    public void deleteNamespace() throws Exception {
        setupDeleteNamespaceTest();

        ecs.deleteNamespace(NAMESPACE);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NamespaceAction.class);
        NamespaceAction.delete(same(connection), nsCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, nsCaptor.getValue());
    }

    /**
     * A user can be created within a specific namespace
     *
     * @throws Exception
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

        ecs.createUser(USER1, NAMESPACE);

        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userCaptor = ArgumentCaptor
                .forClass(String.class);
        PowerMockito.verifyStatic(ObjectUserAction.class);
        ObjectUserAction.create(same(connection), userCaptor.capture(),
                nsCaptor.capture());
        assertEquals(PREFIX + NAMESPACE, nsCaptor.getValue());
        assertEquals(PREFIX + USER1, userCaptor.getValue());
    }

    /**
     * A service can lookup a service definition from the catalog
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testlookupServiceDefinition()
            throws EcsManagementClientException {
        when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        ServiceDefinitionProxy service = ecs
                .lookupServiceDefinition(NAMESPACE_SERVICE_ID);
        assertEquals(NAMESPACE_SERVICE_ID, service.getId());
    }

    /**
     * A lookup of a non-existant service definition ID fails
     *
     * @throws ServiceBrokerException
     */
    @Test(expected = ServiceBrokerException.class)
    public void testLookupMissingServiceDefinitionFails()
            throws ServiceBrokerException, EcsManagementClientException {
        ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID);
    }

    /**
     * A service can lookup a namespace URL using the default base URL in the
     * broker config without SSL, which is default.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testNamespaceURLNoSSLDefaultBaseURL()
            throws EcsManagementClientException {
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);

        when(broker.getBaseUrl()).thenReturn(DEFAULT_BASE_URL_NAME);
        setupBaseUrlTest(DEFAULT_BASE_URL_NAME, true);

        String expectedUrl = HTTP + NAMESPACE + DOT + BASE_URL + _9020;
        Map<String, Object> serviceSettings = plan.getServiceSettings();
        serviceSettings.putAll(service.getServiceSettings());
        assertEquals(expectedUrl, ecs.getNamespaceURL(NAMESPACE, Collections.emptyMap(), serviceSettings));
    }

    /**
     * A service can lookup a namespace URL using the a specific base URL with
     * SSL set in the service.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testNamespaceURLSSLDefaultBaseURL()
            throws EcsManagementClientException {
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        Map<String, Object> serviceSettings = service.getServiceSettings();
        serviceSettings.putAll(plan.getServiceSettings());
        serviceSettings.put(USE_SSL, true);
        service.setServiceSettings(serviceSettings);

        when(broker.getBaseUrl()).thenReturn(DEFAULT_BASE_URL_NAME);
        setupBaseUrlTest(DEFAULT_BASE_URL_NAME, true);
        String expectedUrl = new StringBuilder().append(HTTPS).append(NAMESPACE)
                .append(DOT).append(BASE_URL).append(_9021).toString();
        assertEquals(expectedUrl, ecs.getNamespaceURL(NAMESPACE, Collections.emptyMap(), serviceSettings));
    }

    /**
     * A service can lookup a namespace URL using the parameter supplied base
     * URL, and without SSL.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testNamespaceURLNoSSLParamBaseURL()
            throws EcsManagementClientException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(BASE_URL, BASE_URL_NAME);
        ServiceDefinitionProxy service = namespaceServiceFixture();
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        Map<String, Object> serviceSettings = plan.getServiceSettings();
        serviceSettings.putAll(service.getServiceSettings());

        setupBaseUrlTest(BASE_URL_NAME, true);

        String expectedUrl = HTTP + NAMESPACE + DOT + BASE_URL + _9020;
        assertEquals(expectedUrl, ecs.getNamespaceURL(NAMESPACE, params, serviceSettings));
    }

    /**
     * A service can lookup a namespace URL using the parameter supplied base
     * URL, and with SSL.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testNamespaceURLSSLParamBaseURL()
            throws EcsManagementClientException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(BASE_URL, BASE_URL_NAME);
        ServiceDefinitionProxy service = namespaceServiceFixture();
        Map<String, Object> serviceSettings = service.getServiceSettings();
        serviceSettings.put(USE_SSL, true);
        service.setServiceSettings(serviceSettings);
        PlanProxy plan = service.findPlan(NAMESPACE_PLAN_ID1);
        serviceSettings.putAll(serviceSettings);
        serviceSettings.put(USE_SSL, true);

        setupBaseUrlTest(BASE_URL_NAME, true);

        String expectedURl = HTTPS + NAMESPACE + DOT + BASE_URL + _9021;
        assertEquals(expectedURl, ecs.getNamespaceURL(NAMESPACE, params, serviceSettings));
    }

    /**
     * A service can add an export to a bucket
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testAddNonexistentExportToBucket() throws Exception {
        String absolutePath = "/" + NAMESPACE + "/" + PREFIX + BUCKET_NAME + "/" + EXPORT_NAME;
        PowerMockito.mockStatic(NFSExportAction.class);

        when(NFSExportAction.list(same(connection), eq(absolutePath)))
                .thenReturn(null);

        PowerMockito.doNothing().when(NFSExportAction.class, CREATE, same(connection), eq(absolutePath));

        ecs.addExportToBucket(BUCKET_NAME, EXPORT_NAME);

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
        String absolutePath = "/" + NAMESPACE + "/" + PREFIX + BUCKET_NAME + "/";
        PowerMockito.mockStatic(NFSExportAction.class);

        when(NFSExportAction.list(same(connection), eq(absolutePath)))
                .thenReturn(null);

        PowerMockito.doNothing().when(NFSExportAction.class, CREATE, same(connection), eq(absolutePath));

        ecs.addExportToBucket(BUCKET_NAME, null);

        ArgumentCaptor<String> listPathCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NFSExportAction.class);
        NFSExportAction.list(same(connection), listPathCaptor.capture());
        assertEquals(absolutePath, listPathCaptor.getValue());

        ArgumentCaptor<String> createPathCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(NFSExportAction.class);
        NFSExportAction.create(same(connection), createPathCaptor.capture());
        assertEquals(absolutePath, createPathCaptor.getValue());
    }

    private void setupInitTest() throws EcsManagementClientException {
        DataServiceReplicationGroup rg = new DataServiceReplicationGroup();
        rg.setName(RG_NAME);
        rg.setId(RG_ID);
        UserSecretKey secretKey = new UserSecretKey();

        secretKey.setSecretKey(TEST);
        PowerMockito.mockStatic(BucketAction.class);
        when(BucketAction.exists(connection, REPO_BUCKET, NAMESPACE))
                .thenReturn(true);

        PowerMockito.mockStatic(ReplicationGroupAction.class);
        when(ReplicationGroupAction.list(connection))
                .thenReturn(Collections.singletonList(rg));

        PowerMockito.mockStatic(ObjectUserAction.class);
        when(ObjectUserAction.exists(connection, REPO_USER, NAMESPACE))
                .thenReturn(true);

        PowerMockito.mockStatic(ObjectUserSecretAction.class);
        when(ObjectUserSecretAction.list(connection, REPO_USER))
                .thenReturn(Collections.singletonList(secretKey));
    }

    private void setupBaseUrlTest(String name, boolean namespaceInHost)
            throws EcsManagementClientException {
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
                same(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE),
                eq(limit), eq(warn));
    }

    private void setupCreateBucketTest() throws Exception {
        PowerMockito.mockStatic(BucketAction.class);
        PowerMockito.doNothing().when(BucketAction.class, CREATE,
                same(connection), any(ObjectBucketCreate.class));
    }

    private void setupDeleteBucketQuotaTest() throws Exception {
        PowerMockito.mockStatic(BucketQuotaAction.class);
        PowerMockito.doNothing().when(BucketQuotaAction.class, DELETE,
                same(connection), eq(BUCKET_NAME), eq(NAMESPACE));
    }

    private void setupUpdateNamespaceTest() throws Exception {
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
        PowerMockito.mockStatic(NamespaceAction.class);
        PowerMockito.doNothing().when(NamespaceAction.class, CREATE,
                same(connection), any(NamespaceCreate.class));
    }

    private void setupDeleteNamespaceTest() throws Exception {
        PowerMockito.mockStatic(NamespaceAction.class);
        PowerMockito.doNothing().when(NamespaceAction.class, DELETE,
                same(connection), anyString());
    }

    private void setupCreateNamespaceRetentionTest(boolean exists)
            throws Exception {
        PowerMockito.mockStatic(NamespaceRetentionAction.class);
        PowerMockito
                .when(NamespaceRetentionAction.class, EXISTS, same(connection),
                        anyString(), any(RetentionClassUpdate.class))
                .thenReturn(exists);
        PowerMockito.doNothing().when(NamespaceRetentionAction.class, CREATE,
                same(connection), anyString(), any(RetentionClassCreate.class));
        PowerMockito.doNothing().when(NamespaceRetentionAction.class, UPDATE,
                same(connection), anyString(), anyString(),
                any(RetentionClassUpdate.class));
        PowerMockito.doNothing().when(NamespaceRetentionAction.class, DELETE,
                same(connection), anyString(), anyString());
    }

    private void setupCreateBucketRetentionTest(int retentionPeriod)
            throws Exception {
        DefaultBucketRetention retention = new DefaultBucketRetention();
        retention.setPeriod(retentionPeriod);
        PowerMockito.mockStatic(BucketRetentionAction.class);
        PowerMockito.doNothing().when(BucketRetentionAction.class, UPDATE,
                same(connection), anyString(), anyString(), anyInt());
        PowerMockito.when(BucketRetentionAction.class, GET,
                same(connection), anyString(), anyString())
                .thenReturn(retention);
    }
}