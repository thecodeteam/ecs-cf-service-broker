package com.emc.ecs;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.emc.ecs.managementClient.BaseUrlActionTest;
import com.emc.ecs.managementClient.BucketAclActionTest;
import com.emc.ecs.managementClient.BucketActionTest;
import com.emc.ecs.managementClient.BucketQuotaActionTest;
import com.emc.ecs.managementClient.ConnectionTest;
import com.emc.ecs.managementClient.NamespaceActionTest;
import com.emc.ecs.managementClient.NamespaceQuotaActionTest;
import com.emc.ecs.managementClient.ObjectUserActionTest;
import com.emc.ecs.managementClient.ObjectUserSecretActionTest;
import com.emc.ecs.managementClient.ReplicationGroupActionTest;
import com.emc.ecs.serviceBroker.EcsServiceTest;
import com.emc.ecs.serviceBroker.config.CatalogConfigTest;
import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxyTest;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepositoryTest;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceRepositoryTest;
import com.emc.ecs.serviceBroker.service.EcsServiceInstanceServiceTest;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(Suite.class)
@SuiteClasses({
    BaseUrlActionTest.class,
    BucketAclActionTest.class,
    BucketActionTest.class,
    BucketQuotaActionTest.class,
    ConnectionTest.class,
    NamespaceActionTest.class,
    NamespaceQuotaActionTest.class,
    ObjectUserActionTest.class,
    ObjectUserSecretActionTest.class,
    ReplicationGroupActionTest.class,
    EcsServiceTest.class,
    CatalogConfigTest.class,
    ServiceDefinitionProxyTest.class,
    ServiceInstanceBindingRepositoryTest.class,
    ServiceInstanceRepositoryTest.class,
    EcsServiceInstanceServiceTest.class
    })
public class TestSuite {

    @Rule
    public static WireMockRule wireMockRule = new WireMockRule(
	    WireMockConfiguration.wireMockConfig().port(9020).httpsPort(4443));

    @BeforeClass
    public static void setUp() {
	wireMockRule.start();
    }

    @AfterClass
    public static void tearDown() {
	wireMockRule.shutdownServer();
    }
}