package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.cloud.servicebroker.model.fixture.ServiceInstanceBindingFixture;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.config.Application;
import com.emc.ecs.serviceBroker.service.EcsService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
	initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("development")
public class ServiceInstanceRepositoryTest {
	
	@Autowired
	EcsService ecs;
	
	@Autowired
	ServiceInstanceBindingRepository repository;

	@Test
	public void testSaveFindDelete()
			throws IOException, JAXBException, EcsManagementClientException,
			EcsManagementResourceNotFoundException, URISyntaxException {
		ServiceInstanceBinding binding = bindingInstanceFixture();
		repository.save(binding);
		ServiceInstanceBinding binding2 = repository.find(binding.getBindingId());
		assertEquals(binding.getBindingId(), binding2.getBindingId());
		repository.delete(binding.getBindingId());
	}

	private ServiceInstanceBinding bindingInstanceFixture()
			throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		Map<String, Object> creds = new HashMap<String, Object>();
		creds.put("accessKey", "user");
		creds.put("bucket", "bucket");
		creds.put("secretKey", "password");
		creds.put("endpoint", ecs.getObjectEndpoint());
		ServiceInstanceBinding binding = new ServiceInstanceBinding(
				ServiceInstanceBindingFixture.buildCreateAppBindingRequest());
		binding.setBindingId("service-inst-bind-one-id");
		binding.setCredentials(creds);
		return binding;
	}
}