package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.config.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.emc.ecs.common.Fixtures.serviceInstanceFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class ServiceInstanceBindingRepositoryTest {

    @Autowired
    private ServiceInstanceRepository repository;

    @Test
    public void testSaveFindDelete()
            throws IOException, JAXBException, EcsManagementClientException,
            EcsManagementResourceNotFoundException, URISyntaxException {
        ServiceInstance instance = serviceInstanceFixture();
        repository.save(instance);
        ServiceInstance instance2 = repository
                .find(instance.getServiceInstanceId());
        assertEquals(instance.getServiceInstanceId(),
                instance2.getServiceInstanceId());
        repository.delete(instance.getServiceInstanceId());
    }

    @Test
    public void testFindWithV1Json() throws IOException {
        ServiceInstance serviceInstance = repository.find("service-instance-id-v1");
        assertNotNull(serviceInstance);
    }
}