package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.model.fixture.DataFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.config.Application;
import com.emc.ecs.serviceBroker.config.BrokerConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, initializers = ConfigFileApplicationContextInitializer.class)
public class BucketServiceInstanceServiceTest {
	
	@Autowired
	private EcsService ecs;
	
	@Autowired
	private BrokerConfig broker;
	
	@Test
	public void testSaveFindDelete() throws IOException, JAXBException, EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		ServiceInstanceBindingRepository repository = new ServiceInstanceBindingRepository(broker);
		ServiceInstanceBinding binding = bindingInstanceFixture();
		repository.save(binding);
		ServiceInstanceBinding binding2 = repository.find(binding.getId());
		assertEquals(binding.getId(), binding2.getId());
		repository.delete(binding.getId());
	}
	
	private ServiceInstanceBinding bindingInstanceFixture() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		Map<String, Object> creds = new HashMap<String, Object>();
		creds.put("accessKey", "user");
		creds.put("bucket", "bucket");
		creds.put("secretKey", "password");
		creds.put("endpoint", ecs.getObjectEndpoint());
		return new ServiceInstanceBinding("service-inst-bind-one-id", "binding-one-id", null, null,
				DataFixture.getOrgOneGuid());
	}
}