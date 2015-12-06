package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.fixture.DataFixture;
import org.junit.Test;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;

public class BucketServiceInstanceBindingServiceTest extends RepositoryTest {
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