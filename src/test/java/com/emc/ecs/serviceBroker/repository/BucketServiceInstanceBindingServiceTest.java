package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.cloud.servicebroker.model.fixture.ServiceInstanceFixture;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.config.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("development")
public class BucketServiceInstanceBindingServiceTest {

	@Autowired
	private ServiceInstanceRepository repository;

	@Test
	public void testSaveFindDelete() throws IOException, JAXBException, EcsManagementClientException,
			EcsManagementResourceNotFoundException, URISyntaxException {
		ServiceInstance instance = serviceInstanceFixture();
		repository.save(instance);
		ServiceInstance instance2 = repository.find(instance.getServiceInstanceId());
		assertEquals(instance.getServiceInstanceId(), instance2.getServiceInstanceId());
		repository.delete(instance.getServiceInstanceId());
	}

	private ServiceInstance serviceInstanceFixture() {
		return new ServiceInstance(ServiceInstanceFixture.buildCreateServiceInstanceRequest(false));
	}
}