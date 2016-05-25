package com.emc.ecs.serviceBroker.service;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import static com.emc.ecs.common.Fixtures.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;

import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.config.CatalogConfig;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBinding;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceBindingRepository;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceBindingServiceTest {

    @Mock
    private EcsService ecs;

    @Mock
    ServiceInstanceBindingRepository repository;

    @Mock
    private CatalogConfig catalog;

    @Autowired
    @InjectMocks
    EcsServiceInstanceBindingService bindSvc;

    /**
     * The binding-service can create a user in a namespace, so long as the user
     * doesn't exist.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws EcsManagementClientException
     */
    @Test
    public void testCreateNamespaceUser()
	    throws IOException, JAXBException, EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(false);
	when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
	UserSecretKey userSecretKey = new UserSecretKey();
	userSecretKey.setSecretKey("TEST_KEY");
	when(ecs.createUser(BINDING_ID, NAMESPACE))
		.thenReturn(userSecretKey);

	bindSvc.createServiceInstanceBinding(instanceBindingRequestFixture());
	verify(ecs, times(1)).createUser(BINDING_ID,
		NAMESPACE);
	verify(ecs, times(1)).userExists(BINDING_ID);
	verify(repository).save(any(ServiceInstanceBinding.class));
    }

    /**
     * If the binding-service attempts to create a user that already exists, the
     * service will throw an error.
     * 
     * @throws EcsManagementClientException
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingNamespaceUserFailes()
	    throws EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(true);

	bindSvc.createServiceInstanceBinding(instanceBindingRequestFixture());
    }

    /**
     * The binding-service can remove a user in a namespace.
     * 
     * @throws EcsManagementClientException
     */
    @Test
    public void testRemoveNamespaceUser() throws EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	bindSvc.deleteServiceInstanceBinding(instanceBindingRemoveFixture());
	verify(ecs, times(1)).deleteUser(BINDING_ID);
    }
}