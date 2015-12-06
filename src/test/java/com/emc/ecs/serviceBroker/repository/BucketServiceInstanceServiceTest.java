package com.emc.ecs.serviceBroker.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.model.fixture.DataFixture;
import org.junit.Test;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;

public class BucketServiceInstanceServiceTest extends RepositoryTest {
	@Test
	public void testSaveFindDelete() throws IOException, JAXBException, EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		ServiceInstanceBindingRepository repository = new ServiceInstanceBindingRepository(ecs);
		ServiceInstance binding = bindingInstanceFixture();
		repository.save(binding);
		ServiceInstance binding2 = repository.find(binding.getId());
		assertEquals(binding.getId(), binding2.getId());
		repository.delete(binding.getId());
	}
	
	private ServiceInstanceBinding bindingInstanceFixture() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		Map<String, Object> creds = new HashMap();
		creds.put("accessKey", "user");
		creds.put("bucket", "bucket");
		creds.put("secretKey", "password");
		creds.put("endpoint", ecs.getObjectEndpoint());
		return new ServiceInstanceBinding("service-inst-bind-one-id", "binding-one-id", creds, null,
				DataFixture.getOrgOneGuid());
	}
}