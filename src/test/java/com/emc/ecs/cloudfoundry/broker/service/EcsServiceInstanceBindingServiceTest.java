package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.VolumeMount;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

import static com.emc.ecs.common.Fixtures.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceBindingServiceTest {

    private static final String COLON = ":";
    private static final String HTTP = "http://";
    private static final String SECRET_KEY = "secretKey";
    private static final String TEST_KEY = "6b056992-a14a-4fd1-a642-f44a821a7755";
    private static final String NFS_SCHEME = "nfs://";
    private static final String DRIVER = "nfsv3driver";

    @Mock
    private EcsService ecs;

    @Mock
    private ServiceInstanceBindingRepository repository;

    @Mock
    private ServiceInstanceRepository instanceRepository;

    @Mock
    private CatalogConfig catalog;

    @Autowired
    @InjectMocks
    private EcsServiceInstanceBindingService bindSvc;

    /**
     * The binding-service can create a user in a namespace, so long as the user
     * doesn't exist.
     *
     * @throws JAXBException if there is a JSON serializaton error with repository
     * @throws IOException is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
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
        when(ecs.createUser(BINDING_ID, SERVICE_INSTANCE_ID)).thenReturn(userSecretKey);
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(service);
        when(ecs.getNamespaceURL(eq(SERVICE_INSTANCE_ID),
                any(ServiceDefinitionProxy.class), any(PlanProxy.class),
                eq(Optional.of(new HashMap<>()))))
                .thenReturn("http://ns1.example.com:9020");

        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor
                .forClass(ServiceInstanceBinding.class);
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(instanceRepository.find(SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstanceFixture());
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String s3Url = HTTP + BINDING_ID + COLON + TEST_KEY + "@ns1.example.com:9020";
        assertEquals(s3Url, creds.get("s3Url"));
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(null, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
        verify(ecs, times(1)).createUser(BINDING_ID, SERVICE_INSTANCE_ID);
        verify(ecs, times(1)).userExists(BINDING_ID);
        verify(repository).save(any(ServiceInstanceBinding.class));
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed permissions), so long as the user doesn't exist.
     *
     * @throws JAXBException if there is a JSON serializaton error with repository
     * @throws IOException is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
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
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        doNothing().when(repository).save(bindingCaptor.capture());
        when(instanceRepository.find(SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstanceFixture());

        bindSvc.createServiceInstanceBinding(
                bucketBindingPermissionRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String s3Url = HTTP + BINDING_ID + COLON + TEST_KEY + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID;
        assertEquals(s3Url, creds.get("s3Url"));
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(SERVICE_INSTANCE_ID, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
        verify(ecs, times(1)).createUser(BINDING_ID);
        verify(ecs, times(1)).userExists(BINDING_ID);
        verify(repository).save(any(ServiceInstanceBinding.class));
        List<String> permissions = Arrays.asList("READ", "WRITE");
        verify(ecs, times(1)).addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID),
                eq(permissions));
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed export details), so long as the user doesn't exist.
     *
     * @throws JAXBException if there is a JSON serializaton error with repository
     * @throws IOException is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testCreateBucketUserWithExport()
            throws IOException, JAXBException, EcsManagementClientException {
        when(catalog.findServiceDefinition(eq(BUCKET_SERVICE_ID)))
                .thenReturn(bucketServiceFixture());
        when(ecs.userExists(BINDING_ID)).thenReturn(false);
        when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
        when(ecs.getBucketFileEnabled(anyString())).thenReturn(true);
        when(ecs.getNfsMountHost()).thenReturn("foo");
        UserSecretKey userSecretKey = new UserSecretKey();
        userSecretKey.setSecretKey(TEST_KEY);
        when(ecs.createUser(BINDING_ID)).thenReturn(userSecretKey);
        doThrow(new EcsManagementClientException("Bad request body (1013)"))
                .doNothing()
                .when(ecs)
                .createUserMap(anyString(),anyInt());
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor
                .forClass(ServiceInstanceBinding.class);
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        when(instanceRepository.find(SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstanceFixture());
        doNothing().when(repository).save(bindingCaptor.capture());

        String absolutePath = "/" + NAMESPACE + "/" + SERVICE_INSTANCE_ID + "/" + EXPORT_NAME;

        when(ecs.addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(EXPORT_NAME)))
                .thenReturn(absolutePath);

        bindSvc.createServiceInstanceBinding(
                bucketBindingExportRequestFixture());

        ServiceInstanceBinding binding = bindingCaptor.getValue();
        Map<String, Object> creds = binding.getCredentials();
        String s3Url = HTTP + BINDING_ID + COLON + TEST_KEY + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID;
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(SERVICE_INSTANCE_ID, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
        assertEquals(s3Url, creds.get("s3Url"));

        List<VolumeMount> mounts = binding.getVolumeMounts();
        assertNotNull(mounts);
        String nfsUrl = NFS_SCHEME + "foo" + absolutePath;
        VolumeMount mount = mounts.get(0);
        SharedVolumeDevice device = (SharedVolumeDevice) mount.getDevice();
        Map<String, Object> volumeOpts = device.getMountConfig();

        assertEquals(1, mounts.size());
        assertEquals(DRIVER, mount.getDriver());
        assertEquals(VolumeMount.DeviceType.SHARED, mount.getDeviceType());
        assertEquals("/var/vcap/data", mount.getContainerDir());
        assertEquals(VolumeMount.Mode.READ_WRITE, mount.getMode());
        assertEquals(String.class, device.getVolumeId().getClass());
        assertEquals(nfsUrl, volumeOpts.get("source"));


        verify(ecs, times(1)).createUser(BINDING_ID);
        verify(ecs, times(2)).createUserMap(anyString(), anyInt());
        verify(ecs, times(1)).userExists(BINDING_ID);
        verify(repository).save(any(ServiceInstanceBinding.class));
        verify(ecs, times(1)).addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID));
        verify(ecs, times(1)).addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(EXPORT_NAME));
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed export details of an existing export), so longs as the user doesn't exist.
     */
    @Test
    public void testCreateBucketUserWithExistingExport() {
    }

    /**
     * The binding-service can create a user for a bucket (without parameters to
     * feed permissions), so long as the user doesn't exist.
     *
     * @throws JAXBException if there is a JSON serializaton error with repository
     * @throws IOException is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
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
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(instanceRepository.find(SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstanceFixture());
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(bucketBindingRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String s3Url = HTTP + BINDING_ID + COLON + TEST_KEY + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID;
        assertEquals(s3Url, creds.get("s3Url"));
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(SERVICE_INSTANCE_ID, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
        verify(ecs, times(1)).createUser(BINDING_ID);
        verify(ecs, times(1)).userExists(BINDING_ID);
        verify(ecs, times(1)).addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID));
    }

    /**
     * If a binding is created with the "connect_remote" parameter, set to true, special
     * remote connection credentials are created and returned, to allow a remote Cloud
     * Foundry instance to connect to an existing bucket.
     *
     * @throws EcsManagementClientException when an error occurs with the ECS api
     * @throws IOException upon encountering a serialization error writing to the instance repository
     * @throws JAXBException upon encountering a deserialization error when reading from the instance repository
     */
    @Test
    public void testBucketCreateConnectRemoteService()
            throws EcsManagementClientException, IOException, JAXBException {
        when(ecs.lookupServiceDefinition(eq(BUCKET_SERVICE_ID)))
                .thenReturn(bucketServiceFixture());
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor =
            ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        ArgumentCaptor<ServiceInstance> instanceCaptor =
            ArgumentCaptor .forClass(ServiceInstance.class);

        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        doNothing().when(instanceRepository).save(instanceCaptor.capture());
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(bucketRemoteConnectFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String instanceId = (String) creds.get("instanceId");
        String accessKey = (String) creds.get("accessKey");
        String secretKey = (String) creds.get("secretKey");

        assertEquals(SERVICE_INSTANCE_ID, instanceId);
        assertEquals(BINDING_ID, accessKey);
        assertEquals(String.class, secretKey.getClass());
        assertTrue(instanceCaptor.getValue().remoteConnectionKeyValid(accessKey, secretKey));
        verify(ecs, times(0)).createUser(BINDING_ID);
        verify(ecs, times(0)).userExists(BINDING_ID);
        verify(ecs, times(0)).addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID));
    }

    /**
     * If a binding is created with the "connect_remote" parameter, set to true, special
     * remote connection credentials are created and returned, to allow a remote Cloud
     * Foundry instance to connect to an existing bucket.
     *
     * @throws IOException upon encountering a serialization error writing to the instance repository
     * @throws JAXBException upon encountering a deserialization error when reading from the instance repository
     */
    @Test
    public void testNamespaceCreateConnectRemoteService() throws IOException, JAXBException {
        when(ecs.lookupServiceDefinition(eq(NAMESPACE_SERVICE_ID)))
                .thenReturn(namespaceServiceFixture());
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor =
                ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        ArgumentCaptor<ServiceInstance> instanceCaptor =
                ArgumentCaptor .forClass(ServiceInstance.class);
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        doNothing().when(instanceRepository).save(instanceCaptor.capture());
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(namespaceRemoteConnectFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String instanceId = (String) creds.get("instanceId");
        String accessKey = (String) creds.get("accessKey");
        String secretKey = (String) creds.get("secretKey");

        assertEquals(SERVICE_INSTANCE_ID, instanceId);
        assertEquals(BINDING_ID, accessKey);
        assertTrue(instanceCaptor.getValue().remoteConnectionKeyValid(accessKey, secretKey));
        verify(ecs, times(0)).createUser(BINDING_ID);
        verify(ecs, times(0)).userExists(BINDING_ID);
        verify(ecs, times(0)).addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID));
    }

    /**
     * If the binding-service attempts to create a namespace user that already
     * exists, the service will throw an error.
     *
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingNamespaceUserFails() {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        when(ecs.userExists(BINDING_ID)).thenReturn(true);

        bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());
    }

    /**
     * If the binding-service attempts to create a bucket user that already
     * exists, the service will throw an error.
     *
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingBucketUserFails()
            throws EcsManagementClientException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
        when(ecs.userExists(BINDING_ID)).thenReturn(true);

        bindSvc.createServiceInstanceBinding(
                bucketBindingPermissionRequestFixture());
    }

    /**
     * The binding-service can remove a user in a namespace.
     *
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testRemoveNamespaceUser() throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        when(repository.find(eq(BINDING_ID)))
                .thenReturn(bindingInstanceFixture());
        when(ecs.prefix(NAMESPACE))
                .thenReturn(NAMESPACE);
        when(instanceRepository.find(NAMESPACE))
                .thenReturn(serviceInstanceFixture());
        bindSvc.deleteServiceInstanceBinding(namespaceBindingRemoveFixture());
        verify(ecs, times(1)).deleteUser(BINDING_ID);
        verify(ecs, times(0)).removeUserFromBucket(NAMESPACE, BINDING_ID);
    }

    /**
     * The binding-service can remove a user in a bucket.
     *
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testRemoveBucketUser() throws EcsManagementClientException, IOException, EcsManagementResourceNotFoundException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        when(ecs.getObjectEndpoint())
                .thenReturn(OBJ_ENDPOINT);
        when(repository.find(eq(BINDING_ID)))
                .thenReturn(bindingInstanceFixture());
        when(instanceRepository.find(SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstanceFixture());
        bindSvc.deleteServiceInstanceBinding(bucketBindingRemoveFixture());
        verify(ecs, times(1)).removeUserFromBucket(SERVICE_INSTANCE_ID, BINDING_ID);
        verify(ecs, times(1)).deleteUser(BINDING_ID);
    }

    /**
     * The binding-service can remove a remote access user in a bucket.
     *
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testRemoveBucketRemoteAccess() throws EcsManagementClientException, IOException, EcsManagementResourceNotFoundException, JAXBException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        when(repository.find(eq(BINDING_ID)))
                .thenReturn(bindingRemoteAccessFixture());
        when(instanceRepository.find(eq(SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstanceFixture());

        ServiceInstance inst = serviceInstanceFixture();
        inst.addRemoteConnectionKey(BINDING_ID);
        when(instanceRepository.find(eq(SERVICE_INSTANCE_ID)))
                .thenReturn(inst);

        ArgumentCaptor<ServiceInstance> instanceCaptor =
                ArgumentCaptor .forClass(ServiceInstance.class);
        doNothing().when(instanceRepository).save(instanceCaptor.capture());
        doNothing().when(repository).delete(eq(BINDING_ID));

        bindSvc.deleteServiceInstanceBinding(bucketBindingRemoveFixture());

        assertFalse(instanceCaptor.getValue().remoteConnectionKeyExists(BINDING_ID));
        verify(ecs, times(0)).removeUserFromBucket(SERVICE_INSTANCE_ID, BINDING_ID);
        verify(ecs, times(0)).deleteUser(BINDING_ID);
    }

}