package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.fixture.DataFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.config.Application;
import com.emc.ecs.serviceBroker.config.BrokerConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, initializers = ConfigFileApplicationContextInitializer.class)
public class BucketServiceInstanceBindingServiceTest {
	
	@Autowired
	private BrokerConfig broker;
	
	@Test
	public void testSaveFindDelete() throws IOException, JAXBException, EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		ServiceInstanceRepository repository = new ServiceInstanceRepository(broker);
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