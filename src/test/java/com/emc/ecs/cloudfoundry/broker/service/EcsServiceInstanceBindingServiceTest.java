package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceBindingServiceTest {

    private static final String COLON = ":";

    private static final String HTTP = "http://";

    private static final String SECRET_KEY = "secretKey";

    private static final String TEST_KEY = "TEST_KEY";
    private static final String NFS_SCHEME = "nfs://";
    private static final String DRIVER = "nfsv3driver";
    private static final String VOLUME_GUID = "VOLUME-TEST-GUID";

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
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(NAMESPACE)).thenReturn(NAMESPACE);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(namespaceBindingRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String s3Url = new StringBuilder()
                .append(HTTP)
                .append(BINDING_ID)
                .append(COLON)
                .append(TEST_KEY)
                .append("@ns1.example.com:9020")
                .toString();
        assertEquals(s3Url, creds.get("s3Url"));
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(null, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
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
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(BUCKET_NAME)).thenReturn(BUCKET_NAME);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(
                bucketBindingPermissionRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String s3Url = new StringBuilder()
                .append(HTTP)
                .append(BINDING_ID)
                .append(COLON)
                .append(TEST_KEY)
                .append("@127.0.0.1:9020/")
                .append(BUCKET_NAME)
                .toString();
        assertEquals(s3Url, creds.get("s3Url"));
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(BUCKET_NAME, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
        verify(ecs, times(1)).createUser(BINDING_ID);
        verify(ecs, times(1)).userExists(BINDING_ID);
        verify(repository).save(any(ServiceInstanceBinding.class));
        List<String> permissions = Arrays.asList("READ", "WRITE");
        verify(ecs, times(1)).addUserToBucket(eq(BUCKET_NAME), eq(BINDING_ID),
                eq(permissions));
    }

    /**
     * The binding-service can create a user for a bucket (with parameters to
     * feed export details), so long as the user doesn't exist.
     *
     * @throws JAXBException
     * @throws IOException
     * @throws EcsManagementClientException
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
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        ArgumentCaptor<ServiceInstanceBinding> bindingCaptor = ArgumentCaptor
                .forClass(ServiceInstanceBinding.class);
        when(ecs.prefix(BINDING_ID)).thenReturn(BINDING_ID);
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(BUCKET_NAME)).thenReturn(BUCKET_NAME);
        doNothing().when(repository).save(bindingCaptor.capture());

        String absolutePath = new StringBuilder("/")
                .append(NAMESPACE)
                .append("/")
                .append(BUCKET_NAME)
                .append("/")
                .append(EXPORT_NAME)
                .toString();

        when(ecs.addExportToBucket(eq(BUCKET_NAME), eq(EXPORT_NAME)))
                .thenReturn(absolutePath);

        bindSvc.createServiceInstanceBinding(
                bucketBindingExportRequestFixture());

        ServiceInstanceBinding binding = bindingCaptor.getValue();
        Map<String, Object> creds = binding.getCredentials();
        String s3Url = new StringBuilder()
                .append(HTTP)
                .append(BINDING_ID)
                .append(COLON)
                .append(TEST_KEY)
                .append("@127.0.0.1:9020/")
                .append(BUCKET_NAME)
                .toString();
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(BUCKET_NAME, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
        assertEquals(s3Url, creds.get("s3Url"));

        List<VolumeMount> mounts = binding.getVolumeMounts();
        assertNotNull(mounts);
        String nfsUrl = new StringBuilder()
                .append(NFS_SCHEME)
                .append("foo")
                .append(absolutePath)
                .toString();
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
        verify(ecs, times(1)).userExists(BINDING_ID);
        verify(repository).save(any(ServiceInstanceBinding.class));
        verify(ecs, times(1)).addUserToBucket(eq(BUCKET_NAME), eq(BINDING_ID));
        verify(ecs, times(1)).addExportToBucket(eq(BUCKET_NAME), eq(EXPORT_NAME));
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
        when(ecs.prefix(BINDING_ID + COLON + TEST_KEY))
                .thenReturn(BINDING_ID + COLON + TEST_KEY);
        when(ecs.prefix(BUCKET_NAME)).thenReturn(BUCKET_NAME);
        doNothing().when(repository).save(bindingCaptor.capture());

        bindSvc.createServiceInstanceBinding(bucketBindingRequestFixture());

        Map<String, Object> creds = bindingCaptor.getValue().getCredentials();
        String s3Url = new StringBuilder()
                .append(HTTP)
                .append(BINDING_ID)
                .append(COLON)
                .append(TEST_KEY)
                .append("@127.0.0.1:9020/")
                .append(BUCKET_NAME)
                .toString();
        assertEquals(s3Url, creds.get("s3Url"));
        assertEquals(BINDING_ID, creds.get("accessKey"));
        assertEquals(BUCKET_NAME, creds.get("bucket"));
        assertEquals(TEST_KEY, creds.get(SECRET_KEY));
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