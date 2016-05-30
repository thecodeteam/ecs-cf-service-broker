package com.emc.ecs.cloudfoundry.broker.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import static com.emc.ecs.common.Fixtures.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.cloudfoundry.broker.service.EcsService;
import com.emc.ecs.cloudfoundry.broker.service.EcsServiceInstanceBindingService;
import com.emc.ecs.management.sdk.model.UserSecretKey;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceBindingServiceTest {

    private static final String TEST_KEY = "TEST_KEY";

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
	ServiceDefinitionProxy service = namespaceServiceFixture();
	when(catalog.findServiceDefinition(eq(NAMESPACE_SERVICE_ID)))
		.thenReturn(namespaceServiceFixture());
	when(ecs.userExists(BINDING_ID)).thenReturn(false);
	when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
	UserSecretKey userSecretKey = new UserSecretKey();
	userSecretKey.setSecretKey(TEST_KEY);
	when(ecs.createUser(BINDING_ID, NAMESPACE)).thenReturn(userSecretKey);
	when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
		.thenReturn(service);
	when(ecs.getNamespaceURL(eq(NAMESPACE),
		any(ServiceDefinitionProxy.class), any(PlanProxy.class),
		eq(Optional.of(new HashMap<>()))))
			.thenReturn("http://ns1.example.com:9020");
	ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor
		.forClass(ServiceInstanceBinding.class);
	when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
	when(ecs.prefix(BINDING_ID + ":TEST_KEY")).thenReturn(BINDING_ID + ":TEST_KEY");
	when(ecs.prefix(NAMESPACE)).thenReturn(NAMESPACE);
	doNothing().when(repository).save(bindingCaptor.capture());

	bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());

	Map<String, Object> creds = bindingCaptor .getValue().getCredentials();
	String s3Url = "http://" + BINDING_ID + ":TEST_KEY" + "@ns1.example.com:9020";
	assertEquals(s3Url, creds.get("s3Url"));
	assertEquals(BINDING_ID, creds.get("accessKey"));
	assertEquals(null, creds.get("bucket"));
	assertEquals(TEST_KEY, creds.get("secretKey"));
	verify(ecs, times(1)).createUser(BINDING_ID, NAMESPACE);
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
	userSecretKey.setSecretKey(TEST_KEY);
	when(ecs.createUser(BINDING_ID)).thenReturn(userSecretKey);
	when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());
	ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor
		.forClass(ServiceInstanceBinding.class);
	when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
	when(ecs.prefix(BINDING_ID + ":TEST_KEY")).thenReturn(BINDING_ID + ":TEST_KEY");
	when(ecs.prefix(BUCKET_NAME)).thenReturn(BUCKET_NAME);
	doNothing().when(repository).save(bindingCaptor.capture());

	bindSvc.createServiceInstanceBinding(
		bucketBindingPermissionRequestFixture());

	Map<String, Object> creds = bindingCaptor .getValue().getCredentials();
	String s3Url = "http://" + BINDING_ID + ":TEST_KEY" + "@127.0.0.1:9020/" + BUCKET_NAME;
	assertEquals(s3Url, creds.get("s3Url"));
	assertEquals(BINDING_ID, creds.get("accessKey"));
	assertEquals(BUCKET_NAME, creds.get("bucket"));
	assertEquals(TEST_KEY, creds.get("secretKey"));
	verify(ecs, times(1)).createUser(BINDING_ID);
	verify(ecs, times(1)).userExists(BINDING_ID);
	verify(repository).save(any(ServiceInstanceBinding.class));
	List<String> permissions = Arrays.asList("READ", "WRITE");
	verify(ecs, times(1)).addUserToBucket(eq(BUCKET_NAME), eq(BINDING_ID),
		eq(permissions));
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
	userSecretKey.setSecretKey(TEST_KEY);
	when(ecs.createUser(BINDING_ID)).thenReturn(userSecretKey);
	when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());
	ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor
		.forClass(ServiceInstanceBinding.class);
	when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
	when(ecs.prefix(BINDING_ID + ":TEST_KEY")).thenReturn(BINDING_ID + ":TEST_KEY");
	when(ecs.prefix(BUCKET_NAME)).thenReturn(BUCKET_NAME);
	doNothing().when(repository).save(bindingCaptor.capture());

	bindSvc.createServiceInstanceBinding(bucketBindingRequestFixture());

	Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
	String s3Url = "http://" + BINDING_ID + ":TEST_KEY" + "@127.0.0.1:9020/" + BUCKET_NAME;
	assertEquals(s3Url, creds.get("s3Url"));
	assertEquals(BINDING_ID, creds.get("accessKey"));
	assertEquals(BUCKET_NAME, creds.get("bucket"));
	assertEquals(TEST_KEY, creds.get("secretKey"));
	verify(ecs, times(1)).createUser(BINDING_ID);
	verify(ecs, times(1)).userExists(BINDING_ID);
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

	bindSvc.createServiceInstanceBinding(
		bucketBindingPermissionRequestFixture());
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