package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.EcsManagementAPIConnection;
import com.emc.ecs.management.sdk.actions.*;
import com.emc.ecs.management.sdk.model.BucketTagsParamAdd;
import com.emc.ecs.management.sdk.model.BucketTagsParamUpdate;
import com.emc.ecs.management.sdk.model.DataServiceReplicationGroup;
import com.emc.ecs.management.sdk.model.ObjectBucketCreate;
import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.model.Constants;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.*;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.management.sdk.ManagementAPIConstants.USER;
import static com.emc.ecs.servicebroker.model.Constants.*;
import static com.emc.ecs.servicebroker.service.EcsServiceTest.createListOfTags;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BucketAction.class,
        BucketRetentionAction.class,
        ReplicationGroupAction.class,
        BucketQuotaAction.class,
        BucketTagsAction.class
})
public class BucketTagTests {
    public static final String VALUE1 = "$CTX_NAMESPACE";
    public static final String VALUE2 = "$CTX_CLUSTER_ID";
    public static final String VALUE3 = "$CTX_INSTANCE_NAME";
    public static final String INVALID_VALUE = "$CTX_NAME";

    @Mock
    private BrokerConfig broker;

    @Mock
    private EcsManagementAPIConnection connection;

    @Autowired
    @InjectMocks
    private EcsService ecs;

    @Before
    public void setUp() {
        when(broker.getPrefix()).thenReturn(PREFIX);
        when(broker.getReplicationGroup()).thenReturn(RG_NAME);
        when(broker.getNamespace()).thenReturn(NAMESPACE_NAME);
        when(broker.getRepositoryUser()).thenReturn(USER);
        when(broker.getRepositoryBucket()).thenReturn("repository");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createBucketWithPlaceholderTags() throws Exception {
        setupEsc();
        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> additionalParams = new HashMap<>();
        setupParameters(additionalParams);
        List<Map<String, String>> tags = createListOfTags(KEY1, VALUE1, KEY2, VALUE2, KEY3, VALUE3);
        additionalParams.put(TAGS, tags);
        ecs.createBucket(BUCKET_NAME, CUSTOM_BUCKET_NAME, service, plan, additionalParams);

        List<Map<String, String>> substitutedTags = createListOfTags(KEY1, "testing", KEY2, "644e1dd7-2a7f-18fb-b8ed-ed78c3f92c2b", KEY3, "my-db");
        assertTrue(CollectionUtils.isEqualCollection((List<Map<String, String>>) additionalParams.get(TAGS), substitutedTags));
    }

    @Test
    public void createBucketWithInvalidPlaceholderTags() throws Exception {
        setupEsc();
        ServiceDefinitionProxy service = bucketServiceFixture();
        PlanProxy plan = service.findPlan(BUCKET_PLAN_ID1);

        Map<String, Object> additionalParams = new HashMap<>();

        setupParameters(additionalParams);

        List<Map<String, String>> tags = createListOfTags(KEY1, INVALID_VALUE, KEY2, VALUE2, KEY3, VALUE3);
        additionalParams.put(TAGS, tags);

        Throwable exception = assertThrows(ServiceBrokerException.class, () -> {
            ecs.createBucket(BUCKET_NAME, CUSTOM_BUCKET_NAME, service, plan, additionalParams);
        });

        assertEquals(exception.getMessage(), "Failed to create bucket '" + CUSTOM_BUCKET_NAME + "': Unexpected placeholder : " + INVALID_VALUE);
    }

    private void setupEsc() throws Exception {
        DataServiceReplicationGroup rg1 = new DataServiceReplicationGroup();
        rg1.setName(RG_NAME);
        rg1.setId(RG_ID);

        List<DataServiceReplicationGroup> replicationGroupsList = List.of(rg1);

        PowerMockito.mockStatic(ReplicationGroupAction.class);
        PowerMockito.when(ReplicationGroupAction.class, "list", same(connection))
                .thenReturn(replicationGroupsList);

        PowerMockito.mockStatic(BucketAction.class);
        PowerMockito.doNothing().when(BucketAction.class, "create",
                same(connection), any(ObjectBucketCreate.class));

        PowerMockito.mockStatic(BucketRetentionAction.class);

        PowerMockito.mockStatic(BucketQuotaAction.class);
        PowerMockito.doNothing().when(BucketQuotaAction.class, "create",
                same(connection), eq(PREFIX + BUCKET_NAME), eq(NAMESPACE_NAME),
                anyInt(), anyInt());

        PowerMockito.mockStatic(BucketTagsAction.class);
        PowerMockito.doNothing().when(BucketTagsAction.class, "create", same(connection), anyString(), any(BucketTagsParamAdd.class));
    }

    private void setupParameters(Map<String, Object> additionalParams) {
        additionalParams.put(REPLICATION_GROUP, RG_NAME);

        additionalParams.put(Properties, new HashMap<>() {{
            put("namespace", "testing");
            put("clusterid", "644e1dd7-2a7f-18fb-b8ed-ed78c3f92c2b");
            put("instance_name", "my-db");
        }});
    }
}
