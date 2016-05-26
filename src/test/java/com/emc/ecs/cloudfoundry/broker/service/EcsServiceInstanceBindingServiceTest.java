package com.emc.ecs.cloudfoundry.broker.service;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import static com.emc.ecs.common.Fixtures.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.cloudfoundry.broker.service.EcsService;
import com.emc.ecs.cloudfoundry.broker.service.EcsServiceInstanceBindingService;
import com.emc.ecs.management.sdk.model.UserSecretKey;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceBindingServiceTest {

    @Mock
    private EcsService ecs;

    @Mock
    private ServiceInstanceBindingRepository repository;

    @Mock
    private CatalogConfig catalog;

    @Autowired
    @InjectMocks
    private EcsServiceInstanceBindingService bindSvc;

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
	when(catalog.findServiceDefinition(eq(NAMESPACE_SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(false);
	when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
	UserSecretKey userSecretKey = new UserSecretKey();
	userSecretKey.setSecretKey("TEST_KEY");
	when(ecs.createUser(BINDING_ID, NAMESPACE))
		.thenReturn(userSecretKey);
	when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
		.thenReturn(namespaceServiceFixture());

	bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());
	verify(ecs, times(1)).createUser(BINDING_ID,
		NAMESPACE);
	verify(ecs, times(1)).userExists(BINDING_ID);
	verify(repository).save(any(ServiceInstanceBinding.class));
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed permissions), so long as the user doesn't exist.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws EcsManagementClientException
     */
    @Test
    public void testCreateBucketUserWithPerms()
	    throws IOException, JAXBException, EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(BUCKET_SERVICE_ID)))
		.thenReturn(bucketServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(false);
	when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
	UserSecretKey userSecretKey = new UserSecretKey();
	userSecretKey.setSecretKey("TEST_KEY");
	when(ecs.createUser(BINDING_ID))
		.thenReturn(userSecretKey);
	when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());

	bindSvc.createServiceInstanceBinding(bucketBindingPermissionRequestFixture());
	verify(ecs, times(1)).createUser(BINDING_ID);
	verify(ecs, times(1)).userExists(BINDING_ID);
	verify(repository).save(any(ServiceInstanceBinding.class));
	List<String> permissions = Arrays.asList("READ", "WRITE");
	verify(ecs, times(1)).addUserToBucket(eq(BUCKET_NAME), eq(BINDING_ID),
		Matchers.eq(permissions));
    }

    /**
     * The binding-service can create a user for a bucket (without parameters to
     * feed permissions), so long as the user doesn't exist.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws EcsManagementClientException
     */
    @Test
    public void testCreateBucketUser()
	    throws IOException, JAXBException, EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(BUCKET_SERVICE_ID)))
		.thenReturn(bucketServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(false);
	when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
	UserSecretKey userSecretKey = new UserSecretKey();
	userSecretKey.setSecretKey("TEST_KEY");
	when(ecs.createUser(BINDING_ID))
		.thenReturn(userSecretKey);
	when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());
	bindSvc.createServiceInstanceBinding(bucketBindingRequestFixture());
	verify(ecs, times(1)).createUser(BINDING_ID);
	verify(ecs, times(1)).userExists(BINDING_ID);
	verify(repository).save(any(ServiceInstanceBinding.class));
	verify(ecs, times(1)).addUserToBucket(eq(BUCKET_NAME), eq(BINDING_ID));
    }

    /**
     * If the binding-service attempts to create a namespace user that already
     * exists, the service will throw an error.
     * 
     * @throws EcsManagementClientException
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingNamespaceUserFailes()
	    throws EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(NAMESPACE_SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(true);

	bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());
    }

    /**
     * If the binding-service attempts to create a bucket user that already
     * exists, the service will throw an error.
     * 
     * @throws EcsManagementClientException
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingBucketUserFailes()
	    throws EcsManagementClientException {
	when(catalog.findServiceDefinition(eq(BUCKET_SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(true);

	bindSvc.createServiceInstanceBinding(bucketBindingPermissionRequestFixture());
    }

    /**
     * The binding-service can remove a user in a namespace.
     * 
     * @throws EcsManagementClientException
     */
    @Test
    public void testRemoveNamespaceUser() throws EcsManagementClientException {
	when(catalog.findServiceDefinition(NAMESPACE_SERVICE_ID))
		.thenReturn(namespaceServiceFixture());
	bindSvc.deleteServiceInstanceBinding(namespaceBindingRemoveFixture());
	verify(ecs, times(1)).deleteUser(BINDING_ID);
	verify(ecs, times(0)).removeUserFromBucket(NAMESPACE, BINDING_ID);
    }

    /**
     * The binding-service can remove a user in a bucket.
     * 
     * @throws EcsManagementClientException
     */
    @Test
    public void testRemoveBucketUser() throws EcsManagementClientException {
	when(catalog.findServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());
	bindSvc.deleteServiceInstanceBinding(bucketBindingRemoveFixture());
	verify(ecs, times(1)).removeUserFromBucket(BUCKET_NAME, BINDING_ID);
	verify(ecs, times(1)).deleteUser(BINDING_ID);
    }
}