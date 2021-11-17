package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.Application;
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
import static com.emc.ecs.common.Fixtures.PAGE_SIZE;
import static com.emc.ecs.common.Fixtures.MARKER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class ServiceInstanceRepositoryTest {

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

    @Test
    public void testListServiceInstances() throws IOException {
        ListServiceInstancesResponse response = repository.listServiceInstances(null, 0);
        assertNotNull(response);
    }

    @Test
    public void testListServiceInstancesPageSize() throws IOException {
        ListServiceInstancesResponse response = repository.listServiceInstances(null, PAGE_SIZE);
        assertNotNull(response);
        assertEquals(PAGE_SIZE, response.getPageSize());
    }

    @Test
    public void testListServiceInstancesMarker() throws IOException {
        ListServiceInstancesResponse response = repository.listServiceInstances(MARKER, 0);
        assertNotNull(response);
        assertEquals(MARKER, response.getMarker());
    }

    @Test
    public void testListServiceInstancesArgs() throws IOException {
        ListServiceInstancesResponse response = repository.listServiceInstances(MARKER, PAGE_SIZE);
        assertNotNull(response);
        assertEquals(PAGE_SIZE, response.getPageSize());
        assertEquals(MARKER, response.getMarker());
    }
}