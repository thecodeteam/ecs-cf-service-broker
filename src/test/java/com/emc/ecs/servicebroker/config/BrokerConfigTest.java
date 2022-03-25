package com.emc.ecs.servicebroker.config;

import com.emc.ecs.servicebroker.repository.ListServiceInstanceBindingsResponse;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static com.emc.ecs.common.Fixtures.MARKER;
import static com.emc.ecs.common.Fixtures.PAGE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles({"awsSignatureV4", "test"})
public class BrokerConfigTest {

    @Autowired
    BrokerConfig brokerConfig;

    @Autowired
    private ServiceInstanceBindingRepository repository;

    @Test
    public void S3V4() throws IOException {
        ListServiceInstanceBindingsResponse response = repository.listServiceInstanceBindings(MARKER, PAGE_SIZE);
        assertNotNull(response);
        assertEquals(PAGE_SIZE, response.getPageSize());
        assertEquals(MARKER, response.getMarker());
    }
}
