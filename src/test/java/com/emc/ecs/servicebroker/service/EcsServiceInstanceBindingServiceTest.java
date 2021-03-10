package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.binding.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.servicebroker.model.Constants.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceBindingServiceTest {

    private static final String COLON = ":";
    private static final String HTTP = "http://";
    private static final String TEST_KEY = "6b056992-a14a-4fd1-a642-f44a821a7755";
    private static final String NFS_SCHEME = "nfs://";
    private static final String DRIVER = "nfsv3driver";

    @Mock
    private EcsService ecs;

    @Mock
    private ServiceInstanceBindingRepository repository;

    @Mock
    private ServiceInstanceRepository instanceRepository;

    @InjectMocks
    private EcsServiceInstanceBindingService bindSvc;

    /**
     * The binding-service can create a user in a namespace, so long as the user
     * doesn't exist.
     *
     * @throws JAXBException                if there is a JSON serializaton error with repository
     * @throws IOException                  is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testCreateNamespaceUser() throws IOException, JAXBException, EcsManagementClientException {
        when(ecs.getNamespaceURL(eq(SERVICE_INSTANCE_ID), eq(Collections.emptyMap()), anyMap())).thenReturn("http://ns1.example.com:9020");

        ServiceDefinitionProxy service = namespaceServiceFixture();
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID)).thenReturn(service);

        UserSecretKey userSecretKey = new UserSecretKey();
        userSecretKey.setSecretKey(TEST_KEY);
        when(ecs.createUser(BINDING_ID, SERVICE_INSTANCE_ID)).thenReturn(userSecretKey);

        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY)).thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());

        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();

        assertNull(creds.get(BUCKET));
        assertEquals(BINDING_ID, creds.get(CREDENTIALS_ACCESS_KEY));
        assertEquals(TEST_KEY, creds.get(CREDENTIALS_SECRET_KEY));
        assertEquals(HTTP + BINDING_ID + COLON + TEST_KEY + "@ns1.example.com:9020", creds.get(S3_URL));

        verify(ecs, times(1)).createUser(BINDING_ID, SERVICE_INSTANCE_ID);
        verify(ecs, times(1)).doesUserExist(BINDING_ID, SERVICE_INSTANCE_ID);
        verify(repository).save(any(ServiceInstanceBinding.class));
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed permissions), so long as the user doesn't exist.
     *
     * @throws JAXBException                if there is a JSON serializaton error with repository
     * @throws IOException                  is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testCreateBucketUserWithPerms() throws IOException, JAXBException, EcsManagementClientException {
        when(ecs.doesUserExist(BINDING_ID, NAMESPACE_NAME)).thenReturn(false);
        when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
        UserSecretKey userSecretKey = new UserSecretKey();
        userSecretKey.setSecretKey(TEST_KEY);
        when(ecs.createUser(BINDING_ID, NAMESPACE_NAME)).thenReturn(userSecretKey);
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID)).thenReturn(bucketServiceFixture());
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY)).thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        doNothing().when(repository).save(bindingCaptor.capture());
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());

        bindSvc.createServiceInstanceBinding(bucketBindingPermissionRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();

        assertEquals(SERVICE_INSTANCE_ID, creds.get(BUCKET));
        assertEquals(BINDING_ID, creds.get(CREDENTIALS_ACCESS_KEY));
        assertEquals(TEST_KEY, creds.get(CREDENTIALS_SECRET_KEY));
        assertEquals(HTTP + BINDING_ID + COLON + TEST_KEY + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID, creds.get(S3_URL));

        verify(ecs, times(1)).createUser(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(1)).doesUserExist(BINDING_ID, NAMESPACE_NAME);
        verify(repository).save(any(ServiceInstanceBinding.class));

        List<String> permissions = Arrays.asList("READ", "WRITE");
        verify(ecs, times(1)).addUserToBucket(
                eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(BINDING_ID), eq(permissions)
        );
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed export & volume mount details), so long as the user doesn't exist.
     *
     * @throws JAXBException                if there is a JSON serializaton error with repository
     * @throws IOException                  is unable to serialize JSON to string
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testCreateBucketUserWithExport()
            throws IOException, JAXBException, EcsManagementClientException {
        when(ecs.doesUserExist(BINDING_ID, NAMESPACE_NAME)).thenReturn(false);
        when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
        when(ecs.getBucketFileEnabled(anyString(), anyString())).thenReturn(true);
        when(ecs.getNfsMountHost()).thenReturn("foo");
        UserSecretKey userSecretKey = new UserSecretKey();
        userSecretKey.setSecretKey(TEST_KEY);
        when(ecs.createUser(BINDING_ID, NAMESPACE_NAME)).thenReturn(userSecretKey);
        doThrow(new EcsManagementClientException("Bad request body (1013)"))
                .doNothing()
                .when(ecs)
                .createUserMap(anyString(), anyString(), anyInt());
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

        String absolutePath = "/" + NAMESPACE_NAME + "/" + SERVICE_INSTANCE_ID + "/" + EXPORT_NAME_VALUE;

        when(ecs.addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(EXPORT_NAME_VALUE)))
                .thenReturn(absolutePath);

        bindSvc.createServiceInstanceBinding(bucketBindingExportRequestFixture());

        ServiceInstanceBinding binding = bindingCaptor.getValue();
        Map<String, Object> creds = binding.getCredentials();

        assertEquals(SERVICE_INSTANCE_ID, creds.get(BUCKET));
        assertEquals(BINDING_ID, creds.get(CREDENTIALS_ACCESS_KEY));
        assertEquals(TEST_KEY, creds.get(CREDENTIALS_SECRET_KEY));
        assertEquals(HTTP + BINDING_ID + COLON + TEST_KEY + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID, creds.get(S3_URL));

        List<VolumeMount> mounts = binding.getVolumeMounts();
        assertNotNull(mounts);
        String nfsUrl = NFS_SCHEME + "foo" + absolutePath;
        VolumeMount mount = mounts.get(0);
        SharedVolumeDevice device = (SharedVolumeDevice) mount.getDevice();
        Map<String, Object> volumeOpts = device.getMountConfig();

        assertEquals(1, mounts.size());
        assertEquals(DRIVER, mount.getDriver());
        assertEquals(VOLUME_MOUNT_VALUE, mount.getContainerDir());
        assertEquals(VolumeMount.DeviceType.SHARED, mount.getDeviceType());
        assertEquals(VolumeMount.Mode.READ_WRITE, mount.getMode());
        assertEquals(String.class, device.getVolumeId().getClass());
        assertEquals(nfsUrl, volumeOpts.get(VOLUME_EXPORT_SOURCE));


        verify(ecs, times(1)).createUser(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(2)).createUserMap(anyString(),anyString(), anyInt());
        verify(ecs, times(1)).doesUserExist(BINDING_ID, NAMESPACE_NAME);
        verify(repository).save(any(ServiceInstanceBinding.class));
        verify(ecs, times(1)).addUserToBucket(eq(SERVICE_INSTANCE_ID), anyString(), eq(BINDING_ID));
        verify(ecs, times(1)).addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(EXPORT_NAME_VALUE));
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
     * @throws IOException   is unable to serialize JSON to string
     */
    @Test
    public void testCreateBucketUser() throws IOException, JAXBException {
        when(ecs.doesUserExist(BINDING_ID, NAMESPACE_NAME)).thenReturn(false);
        when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);
        UserSecretKey userSecretKey = new UserSecretKey();
        userSecretKey.setSecretKey(TEST_KEY);
        when(ecs.createUser(BINDING_ID, NAMESPACE_NAME)).thenReturn(userSecretKey);
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID)).thenReturn(bucketServiceFixture());
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY)).thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(bucketBindingRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();

        assertEquals(SERVICE_INSTANCE_ID, creds.get(BUCKET));
        assertEquals(BINDING_ID, creds.get(CREDENTIALS_ACCESS_KEY));
        assertEquals(TEST_KEY, creds.get(CREDENTIALS_SECRET_KEY));
        assertEquals(HTTP + BINDING_ID + COLON + TEST_KEY + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID, creds.get(S3_URL));

        verify(ecs, times(1)).createUser(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(1)).doesUserExist(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(1)).addUserToBucket(eq(SERVICE_INSTANCE_ID), anyString(), eq(BINDING_ID));
    }

    /**
     * If a binding is created with the "remote_connection" parameter, set to true, special
     * remote connection credentials are created and returned, to allow a remote Cloud
     * Foundry instance to connect to an existing bucket.
     *
     * @throws IOException   upon encountering a serialization error writing to the instance repository
     * @throws JAXBException upon encountering a deserialization error when reading from the instance repository
     */
    @Test
    public void testBucketCreateConnectRemoteService() throws IOException, JAXBException {
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        ArgumentCaptor<ServiceInstance> instanceCaptor = ArgumentCaptor.forClass(ServiceInstance.class);

        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        doNothing().when(instanceRepository).save(instanceCaptor.capture());
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(bucketRemoteConnectFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String instanceId = (String) creds.get(CREDENTIALS_INSTANCE_ID);
        String accessKey = (String) creds.get(CREDENTIALS_ACCESS_KEY);
        String secretKey = (String) creds.get(CREDENTIALS_SECRET_KEY);

        assertEquals(SERVICE_INSTANCE_ID, instanceId);
        assertEquals(BINDING_ID, accessKey);
        assertEquals(String.class, secretKey.getClass());
        assertTrue(instanceCaptor.getValue().remoteConnectionKeyValid(accessKey, secretKey));
        verify(ecs, times(0)).createUser(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(0)).doesUserExist(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(0)).addUserToBucket(eq(SERVICE_INSTANCE_ID), anyString(), eq(BINDING_ID));
    }

    /**
     * If a binding is created with the "remote_connection" parameter, set to true, special
     * remote connection credentials are created and returned, to allow a remote Cloud
     * Foundry instance to connect to an existing bucket.
     *
     * @throws IOException   upon encountering a serialization error writing to the instance repository
     * @throws JAXBException upon encountering a deserialization error when reading from the instance repository
     */
    @Test
    public void testNamespaceCreateConnectRemoteService() throws IOException, JAXBException {
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor.forClass(ServiceInstanceBinding.class);
        ArgumentCaptor<ServiceInstance> instanceCaptor = ArgumentCaptor.forClass(ServiceInstance.class);
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        doNothing().when(instanceRepository).save(instanceCaptor.capture());
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(namespaceRemoteConnectFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String instanceId = (String) creds.get(CREDENTIALS_INSTANCE_ID);
        String accessKey = (String) creds.get(CREDENTIALS_ACCESS_KEY);
        String secretKey = (String) creds.get(CREDENTIALS_SECRET_KEY);

        assertEquals(SERVICE_INSTANCE_ID, instanceId);
        assertEquals(BINDING_ID, accessKey);
        assertTrue(instanceCaptor.getValue().remoteConnectionKeyValid(accessKey, secretKey));
        verify(ecs, times(0)).createUser(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(0)).doesUserExist(BINDING_ID, NAMESPACE_NAME);
        verify(ecs, times(0)).addUserToBucket(eq(SERVICE_INSTANCE_ID), anyString(), eq(BINDING_ID));
    }

    /**
     * If the binding-service attempts to create a namespace user that already
     * exists, the service will throw an error.
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingNamespaceUserFails() throws IOException {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID)).thenReturn(namespaceServiceFixture());
        when(ecs.prefix(SERVICE_INSTANCE_ID)).thenReturn(SERVICE_INSTANCE_ID);
        when(ecs.doesUserExist(BINDING_ID, SERVICE_INSTANCE_ID)).thenReturn(true);

        bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());
        verify(ecs, times(0)).createUser(BINDING_ID, SERVICE_INSTANCE_ID);
    }

    /**
     * If the binding-service attempts to create a bucket user that already
     * exists, the service will throw an error.
     */
    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void testCreateExistingBucketUserFails() throws IOException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID)).thenReturn(bucketServiceFixture());
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        when(ecs.doesUserExist(BINDING_ID, NAMESPACE_NAME)).thenReturn(true);

        bindSvc.createServiceInstanceBinding(
                bucketBindingPermissionRequestFixture());
    }

    /**
     * The binding-service can remove a user in a namespace.
     */
    @Test
    public void testRemoveNamespaceUser() throws EcsManagementClientException, IOException {
        ServiceInstanceBinding bindingFixture = bindingInstanceFixture();

        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID)).thenReturn(namespaceServiceFixture());
        when(ecs.prefix(NAMESPACE_NAME)).thenReturn(PREFIX + NAMESPACE_NAME);
        when(repository.find(eq(BINDING_ID))).thenReturn(bindingFixture);

        bindSvc.deleteServiceInstanceBinding(namespaceBindingRemoveFixture());

        verify(ecs, times(1)).deleteUser(bindingFixture.getName(), PREFIX + NAMESPACE_NAME);
        verify(ecs, times(0)).removeUserFromBucket(NAMESPACE_NAME, NAMESPACE_NAME, bindingFixture.getName());
    }

    /**
     * The binding-service can remove a user in a bucket.
     */
    @Test
    public void testRemoveBucketUser() throws EcsManagementClientException, IOException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID)).thenReturn(bucketServiceFixture());
        ServiceInstanceBinding bindingFixture = bindingInstanceFixture();
        when(repository.find(eq(BINDING_ID))).thenReturn(bindingFixture);
        when(instanceRepository.find(SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceFixture());
        bindSvc.deleteServiceInstanceBinding(bucketBindingRemoveFixture());
        verify(ecs, times(1)).removeUserFromBucket(SERVICE_INSTANCE_ID, NAMESPACE_NAME, bindingFixture.getName());
        verify(ecs, times(1)).deleteUser(bindingFixture.getName(), NAMESPACE_NAME);
    }

    /**
     * The binding-service can remove a remote access user in a bucket.
     *
     * @throws EcsManagementClientException if ecs management API returns an error
     */
    @Test
    public void testRemoveBucketRemoteAccess() throws EcsManagementClientException, IOException {
        when(repository.find(eq(BINDING_ID))).thenReturn(bindingRemoteAccessFixture());

        ServiceInstance inst = serviceInstanceFixture();
        inst.addRemoteConnectionKey(BINDING_ID);
        when(instanceRepository.find(eq(SERVICE_INSTANCE_ID))).thenReturn(inst);

        ArgumentCaptor<ServiceInstance> instanceCaptor = ArgumentCaptor.forClass(ServiceInstance.class);
        doNothing().when(instanceRepository).save(instanceCaptor.capture());
        doNothing().when(repository).delete(eq(BINDING_ID));

        bindSvc.deleteServiceInstanceBinding(bucketBindingRemoveFixture());

        assertFalse(instanceCaptor.getValue().remoteConnectionKeyExists(BINDING_ID));
        verify(ecs, times(0)).removeUserFromBucket(SERVICE_INSTANCE_ID, NAMESPACE_NAME, BINDING_ID);
        verify(ecs, times(0)).deleteUser(BINDING_ID, NAMESPACE_NAME);
    }

}
