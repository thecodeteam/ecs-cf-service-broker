package com.emc.ecs.managementClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.tomakehurst.wiremock.WireMockServer;

@RunWith(Suite.class)
@SuiteClasses({ BaseUrlActionTest.class, BucketAclActionTest.class,
		BucketActionTest.class, BucketQuotaActionTest.class,
		ConnectionTest.class, ObjectUserActionTest.class,
		ObjectUserSecretActionTest.class, ReplicationGroupActionTest.class })
public class ClientTestSuite {
	
	private static WireMockServer wireMockServer;

	@BeforeClass
	public static void setUp() {
		wireMockServer = new WireMockServer(
				wireMockConfig().httpsPort(8443));
		wireMockServer.start();
	}
	
	@AfterClass
	public static void cleanup() {
		wireMockServer.stop();
	}
}
