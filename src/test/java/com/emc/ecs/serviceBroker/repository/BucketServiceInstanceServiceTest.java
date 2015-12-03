package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.fixture.DataFixture;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;

public class BucketServiceInstanceServiceTest {
	protected URL certificate = getClass().getClassLoader().getResource("localhost.pem");
	protected Connection connection = new Connection("https://104.197.254.237:4443", "root", "ChangeMe", certificate);
	protected String namespace = "ns1";
	protected String replicationGroup = "urn:storageos:ReplicationGroupInfo:f81a7335-cadf-48fb-8eda-4856b250e9de:global";
	protected EcsRepositoryCredentials creds = 
		new EcsRepositoryCredentials("ecs-cf-service-broker-repository",
			"ecs-cf-service-broker-repository", namespace, replicationGroup);
	protected EcsService ecs;
	
	@Before
	public void setUp() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		ecs = new EcsService(connection, creds);
	}

	@Test
	public void testSaveFindDelete() throws IOException, JAXBException, EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		ServiceInstanceRepository repository = new ServiceInstanceRepository(ecs);
		ServiceInstance instance = serviceInstanceFixture();
		repository.save(instance);
		ServiceInstance instance2 = repository.find(instance.getId());
		assertEquals(instance.getId(), instance2.getId());
		repository.delete(instance.getId());
	}
	
	private ServiceInstance serviceInstanceFixture() {
		return new ServiceInstance("service-inst-one-id", "service-one-id", "plan-one-id",
				DataFixture.getOrgOneGuid(), DataFixture.getSpaceOneGuid(), null);
	}
}
