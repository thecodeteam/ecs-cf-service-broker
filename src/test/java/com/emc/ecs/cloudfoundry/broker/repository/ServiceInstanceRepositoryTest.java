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

import static com.emc.ecs.common.Fixtures.bindingInstanceFixture;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class ServiceInstanceRepositoryTest {

    @Autowired
    private ServiceInstanceBindingRepository repository;

    @Test
    public void testSaveFindDelete()
            throws IOException, JAXBException, EcsManagementClientException,
            EcsManagementResourceNotFoundException, URISyntaxException {
        ServiceInstanceBinding binding = bindingInstanceFixture();
        repository.save(binding);
        ServiceInstanceBinding binding2 = repository
                .find(binding.getBindingId());
        assertEquals(binding.getBindingId(), binding2.getBindingId());
        repository.delete(binding.getBindingId());
    }
}