package com.emc.ecs.serviceBroker.repository;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.tomakehurst.wiremock.WireMockServer;

@RunWith(Suite.class)
@SuiteClasses({ BucketServiceInstanceServiceTest.class,
		BucketServiceInstanceBindingServiceTest.class })
public class RepositoryTestSuite {
	
	private static WireMockServer wireMockServer;

	@BeforeClass
	public static void setUp() {
		wireMockServer = new WireMockServer(
				wireMockConfig()
					.port(9020) 	   // use HTTP port for s3
					.httpsPort(8443)); // use HTTPS port for ecs-management 
		wireMockServer.start();
	}
	
	@AfterClass
	public static void cleanup() {
		wireMockServer.stop();
	}

}
