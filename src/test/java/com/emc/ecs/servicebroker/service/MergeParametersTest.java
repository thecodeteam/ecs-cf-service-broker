package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.servicebroker.model.Constants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@SuppressWarnings("unchecked")
public class MergeParametersTest {
    @Mock
    private BrokerConfig broker;

    private Map<String, Object> brokerSettings = new HashMap<>();
    {
        brokerSettings.put(NAMESPACE, NAMESPACE_NAME);
        brokerSettings.put(REPLICATION_GROUP, RG_NAME);
        brokerSettings.put(BASE_URL, DEFAULT_BASE_URL_NAME);
        brokerSettings.put(USE_SSL, true);
    }

    private static ServiceDefinitionProxy SERVICE_DEFINITION = bucketServiceWithSettingOverridesFixture();

    @Before
    public void setUp() {
        when(broker.getSettings()).thenReturn(brokerSettings);
    }

    @Test
    public void parametersMergeWithEmptyPlanAndAdditionalParams() throws Exception {
        Map<String, Object> result = EcsService.mergeParameters(broker, SERVICE_DEFINITION, SERVICE_DEFINITION.findPlan(BUCKET_PLAN_ID1), new HashMap<>());

        assertNotNull("Broker settings are used as merge defaults", result.get(USE_SSL));
        assertTrue("Broker settings are not overwritten with empty params, plan and service settings", (Boolean) result.get(USE_SSL));

        assertEquals("Plan settings overrides broker settings", BASE_URL_NAME, result.get(BASE_URL));
        assertEquals("Service settings overrides broker settings", RG_NAME_3, result.get(REPLICATION_GROUP));

        assertEquals("Service settings are defaults when plan, broker and additional settings are empty", ServiceType.BUCKET.getAlias(), result.get(SERVICE_TYPE));
        assertEquals("Broker settings are used as merge defaults", NAMESPACE_NAME, result.get(NAMESPACE));
    }

    @Test
    public void serviceSettingsOverwritesAll() throws Exception {
        Map<String, Object> requestParameters = new HashMap<>();
        requestParameters.put(REPLICATION_GROUP, RG_NAME_4);

        Map<String, Object> result = EcsService.mergeParameters(broker, SERVICE_DEFINITION, SERVICE_DEFINITION.findPlan(BUCKET_PLAN_ID2), requestParameters);

        assertEquals("Service settings overwrites plan, additional params and broker", RG_NAME_3, result.get(REPLICATION_GROUP));
    }

    @Test
    public void serviceSettingsOverwritesAdditionalParameters() throws Exception {
        Map<String, Object> requestParameters = new HashMap<>();
        requestParameters.put(DEFAULT_RETENTION, 500);

        Map<String, Object> result = EcsService.mergeParameters(broker, SERVICE_DEFINITION, SERVICE_DEFINITION.findPlan(BUCKET_PLAN_ID1), requestParameters);

        assertEquals("Service settings overwrites additional params", 100, result.get(DEFAULT_RETENTION));
    }

    @Test
    public void planSettingsOverwritesBroker() throws Exception {
        Map<String, Object> result = EcsService.mergeParameters(broker, SERVICE_DEFINITION, SERVICE_DEFINITION.findPlan(BUCKET_PLAN_ID3), new HashMap<>());
        assertEquals("Broker settings are used as merge defaults", NAMESPACE_NAME_2, result.get(NAMESPACE));
    }

    @Test
    public void additionalParametersOverwritesBroker() throws Exception {
        Map<String, Object> requestParameters = new HashMap<>();
        requestParameters.put(NAMESPACE, NAMESPACE_NAME_2);

        Map<String, Object> result = EcsService.mergeParameters(broker, SERVICE_DEFINITION, SERVICE_DEFINITION.findPlan(BUCKET_PLAN_ID1), requestParameters);
        assertEquals("Request parameters overwrites broker settings", NAMESPACE_NAME_2, result.get(NAMESPACE));
    }

    @Test
    public void planSettingsOverwritesRequest() throws Exception {
        Map<String, Object> quota = new HashMap<>();
        quota.put(QUOTA_WARN, 100);
        quota.put(QUOTA_LIMIT, 500);
        Map<String, Object> requestParameters = new HashMap<>();

        requestParameters.put(REPLICATION_GROUP, RG_NAME_4);
        requestParameters.put(QUOTA, quota);

        Map<String, Object> result = EcsService.mergeParameters(broker, SERVICE_DEFINITION, SERVICE_DEFINITION.findPlan(BUCKET_PLAN_ID1), requestParameters);
        assertNotNull(result.get(QUOTA));

        Map<String, Object> actualQuota = (Map<String, Object>) result.get(QUOTA);

        assertNotEquals("Plan settings overrides additional request parameters", quota, actualQuota);
        assertEquals(4, actualQuota.get(QUOTA_WARN));
        assertEquals(5, actualQuota.get(QUOTA_LIMIT));
    }

}
