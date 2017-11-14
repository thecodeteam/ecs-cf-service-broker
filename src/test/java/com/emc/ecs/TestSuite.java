package com.emc.ecs;

import com.emc.ecs.cloudfoundry.broker.config.CatalogConfigTest;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxyTest;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepositoryTest;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepositoryTest;
import com.emc.ecs.cloudfoundry.broker.service.BucketBindingWorkflowTest;
import com.emc.ecs.cloudfoundry.broker.service.EcsServiceInstanceBindingServiceTest;
import com.emc.ecs.cloudfoundry.broker.service.EcsServiceInstanceServiceTest;
import com.emc.ecs.cloudfoundry.broker.service.EcsServiceTest;
import com.emc.ecs.management.sdk.*;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        BaseUrlActionTest.class,
        BucketAclActionTest.class,
        BucketActionTest.class,
        BucketQuotaActionTest.class,
        BucketRetentionActionTest.class,
        ConnectionTest.class,
        NamespaceActionTest.class,
        NamespaceQuotaActionTest.class,
        NamespaceRetentionActionTest.class,
        NFSExportActionTest.class,
        ObjectUserActionTest.class,
        ObjectUserSecretActionTest.class,
        ReplicationGroupActionTest.class,
        EcsServiceTest.class,
        CatalogConfigTest.class,
        ServiceDefinitionProxyTest.class,
        ServiceInstanceBindingRepositoryTest.class,
        ServiceInstanceRepositoryTest.class,
        EcsServiceInstanceBindingServiceTest.class,
        EcsServiceInstanceServiceTest.class,
        BucketBindingWorkflowTest.class
    })
public class TestSuite {

    @Rule
    public static WireMockRule wireMockRuleMgmt = new WireMockRule(
        WireMockConfiguration.wireMockConfig()
            .httpsPort(4443)
            .usingFilesUnderClasspath("wiremockMgmt"));

    @Rule
    public static WireMockRule wireMockRuleS3 = new WireMockRule(
        WireMockConfiguration.wireMockConfig()
            .port(9020)
            //.httpsPort(9021)
            .usingFilesUnderClasspath("wiremockS3"));

    @BeforeClass
    public static void setUp() {
        wireMockRuleMgmt.start();
        wireMockRuleS3.start();
    }

    @AfterClass
    public static void tearDown() {
        wireMockRuleMgmt.shutdownServer();
        wireMockRuleS3.shutdownServer();
    }
}