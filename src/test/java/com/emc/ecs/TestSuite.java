package com.emc.ecs;

import com.emc.ecs.management.sdk.actions.*;
import com.emc.ecs.servicebroker.config.CatalogConfigTest;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxyTest;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepositoryTest;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepositoryTest;
import com.emc.ecs.management.sdk.*;
import com.emc.ecs.servicebroker.service.*;
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
        EcsManagementAPIConnectionTest.class,
        NamespaceActionTest.class,
        NamespaceQuotaActionTest.class,
        NamespaceRetentionActionTest.class,
        NFSExportActionTest.class,
        ObjectUserActionTest.class,
        ObjectUserSecretActionTest.class,
        ReplicationGroupActionTest.class,
        MergeParametersTest.class,
        MetadataSearchValidationTests.class,
        EcsServiceTest.class,
        CatalogConfigTest.class,
        ServiceDefinitionProxyTest.class,
        ServiceInstanceBindingRepositoryTest.class,
        ServiceInstanceRepositoryTest.class,
        EcsServiceInstanceBindingServiceTest.class,
        EcsServiceInstanceServiceTest.class,
        BucketBindingWorkflowTest.class,
        BucketInstanceWorkflowTest.class,
        RemoteConnectionInstanceWorkflowTest.class
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