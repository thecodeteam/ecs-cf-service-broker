package com.emc.ecs.cloudfoundry.broker.repository;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.Application;
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
}