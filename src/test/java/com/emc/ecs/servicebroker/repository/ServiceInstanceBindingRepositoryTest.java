package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
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
import java.util.Map;

import static com.emc.ecs.common.Fixtures.bindingInstanceFixture;
import static com.emc.ecs.common.Fixtures.PAGE_SIZE;
import static com.emc.ecs.common.Fixtures.MARKER;
import static com.emc.ecs.servicebroker.model.Constants.CREDENTIALS_SECRET_KEY;
import static com.emc.ecs.servicebroker.model.Constants.S3_URL;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class ServiceInstanceBindingRepositoryTest {

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

    @Test
    public void testRemoveSecretCredentials() {
        ServiceInstanceBinding binding = bindingInstanceFixture();
        Map<String, Object> credentials = binding.getCredentials();
        assertTrue(credentials.containsKey(CREDENTIALS_SECRET_KEY));
        assertTrue(credentials.containsKey(S3_URL));

        ServiceInstanceBinding bindingWithoutSecrets = repository.removeSecretCredentials(binding);
        Map<String, Object> credentialsWithoutSecrets = bindingWithoutSecrets.getCredentials();
        assertFalse(credentialsWithoutSecrets.containsKey(CREDENTIALS_SECRET_KEY));
        assertFalse(credentialsWithoutSecrets.containsKey(S3_URL));
    }

    @Test
    public void testListServiceInstanceBindings() throws IOException {
        ListServiceInstanceBindingsResponse response = repository.listServiceInstanceBindings(null, 0);
        assertNotNull(response);
    }

    @Test
    public void testListServiceInstanceBindingsPageSize() throws IOException {
        ListServiceInstanceBindingsResponse response = repository.listServiceInstanceBindings(null, PAGE_SIZE);
        assertNotNull(response);
        assertEquals(PAGE_SIZE, response.getPageSize());
    }

    @Test
    public void testListServiceInstanceBindingsMarker() throws IOException {
        ListServiceInstanceBindingsResponse response = repository.listServiceInstanceBindings(MARKER, 0);
        assertNotNull(response);
        assertEquals(MARKER, response.getMarker());
    }

    @Test
    public void testListServiceInstanceBindingsArgs() throws IOException {
        ListServiceInstanceBindingsResponse response = repository.listServiceInstanceBindings(MARKER, PAGE_SIZE);
        assertNotNull(response);
        assertEquals(PAGE_SIZE, response.getPageSize());
        assertEquals(MARKER, response.getMarker());
    }
}