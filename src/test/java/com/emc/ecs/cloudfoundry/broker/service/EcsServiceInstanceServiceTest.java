package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceServiceTest {

    @Mock
    private EcsService ecs;

    @Mock
    private ServiceInstanceRepository repository;

    @Autowired
    @InjectMocks
    private EcsServiceInstanceService instSvc;

    /**
     * The instance-service can create a bucket with empty params.
     *
     * @throws EcsManagementClientException
     * @throws JAXBException
     * @throws IOException
     * @throws EcsManagementResourceNotFoundException
     */
    @Test
    public void testCreateBucketService() throws Exception {
        ServiceDefinitionProxy service = bucketServiceFixture();
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(service);
        Map<String, Object> params = new HashMap<>();
        instSvc.createServiceInstance(bucketCreateRequestFixture(params));

        verify(repository).save(any(ServiceInstance.class));
        verify(ecs, times(1)).createBucket(eq(BUCKET_NAME),
                eq(service), any(PlanProxy.class),
                eq(params));
    }

    /**
     * The instance-service can create a bucket with remote connection params
     *
     * @throws EcsManagementClientException
     * @throws JAXBException
     * @throws IOException
     * @throws EcsManagementResourceNotFoundException
     */
    @Test
    public void testCreateRemoteBucketService() throws Exception {
        ServiceDefinitionProxy service = bucketServiceFixture();
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(service);

        Map<String, Object> params = new HashMap<>();
        ServiceInstance repoInst = new ServiceInstance(bucketCreateRequestFixture(params));
        repoInst.addRemoteConnectionKey(BINDING_ID, REMOTE_CONNECT_KEY);
        when(repository.find(BUCKET_NAME))
                .thenReturn(repoInst);

        Map<String, Object> remoteConnection = new HashMap<>();
        remoteConnection.put("accessKey", BINDING_ID);
        remoteConnection.put("secretKey", REMOTE_CONNECT_KEY);
        remoteConnection.put("instanceId", BUCKET_NAME);
        params.put("remote_connection", remoteConnection);

        instSvc.createServiceInstance(remoteBucketCreateRequestFixture(params));

        ArgumentCaptor<ServiceInstance> serviceInstanceCaptor = ArgumentCaptor.forClass(ServiceInstance.class);

        verify(ecs, times(0)).createBucket(any(), any(), any(), any());
        verify(repository, times(2)).save(serviceInstanceCaptor.capture());

        List<ServiceInstance> instances = serviceInstanceCaptor.getAllValues();

        assertEquals(BUCKET_NAME, instances.get(0).getName());
        assertEquals(BUCKET_NAME, instances.get(0).getServiceInstanceId());
        assertTrue(instances.get(0).getReferences().contains(BUCKET_NAME));
        assertTrue(instances.get(0).getReferences().contains(SERVICE_INSTANCE_ID));

        assertEquals(BUCKET_NAME, instances.get(1).getName());
        assertEquals(SERVICE_INSTANCE_ID, instances.get(1).getServiceInstanceId());
        assertTrue(instances.get(1).getReferences().contains(BUCKET_NAME));
        assertTrue(instances.get(1).getReferences().contains(SERVICE_INSTANCE_ID));

    }

    /**
     * The instance-service can delete a bucket with empty params.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testDeleteBucketService() throws EcsManagementClientException, IOException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        Map<String, Object> params = new HashMap<>();
        when(repository.find(BUCKET_NAME))
                .thenReturn(new ServiceInstance(bucketCreateRequestFixture(params)));

        instSvc.deleteServiceInstance(bucketDeleteRequestFixture());

        verify(repository, times(1)).delete(BUCKET_NAME);
        verify(ecs, times(1)).deleteBucket(BUCKET_NAME);
    }

    /**
     * The instance-service can delete a bucket with empty params.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testDeleteBucketServiceWithRemoteConnection() throws EcsManagementClientException, IOException, JAXBException {
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        Map<String, Object> params = new HashMap<>();
        ServiceInstance inst = new ServiceInstance(bucketCreateRequestFixture(params));
        ArgumentCaptor<ServiceInstance> instanceArgumentCaptor = ArgumentCaptor.forClass(ServiceInstance.class);
        inst.addReference(SERVICE_INSTANCE_ID);
        when(repository.find(BUCKET_NAME)).thenReturn(inst);
        when(repository.find(SERVICE_INSTANCE_ID)).thenReturn(inst);
        doNothing().when(repository).save(instanceArgumentCaptor.capture());

        instSvc.deleteServiceInstance(bucketDeleteRequestFixture());

        verify(repository, times(0)).delete(SERVICE_INSTANCE_ID);
        verify(repository, times(1)).save(any(ServiceInstance.class));
        verify(repository, times(1)).delete(BUCKET_NAME);
        verify(ecs, times(0)).deleteBucket(BUCKET_NAME);

        ServiceInstance reference = instanceArgumentCaptor.getValue();
        assertEquals(1, reference.getReferences().size());
        assertTrue(reference.getReferences().contains(SERVICE_INSTANCE_ID));
    }

    /**
     * The instance-service can change a bucket's plan with empty params.
     *
     * @throws IOException
     * @throws JAXBException
     * @throws EcsManagementClientException
     */
    @Test
    public void testChangeBucketService()
            throws IOException, JAXBException, EcsManagementClientException {
        Map<String, Object> params = new HashMap<>();
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        when(repository.find(BUCKET_NAME)).thenReturn(
                new ServiceInstance(bucketCreateRequestFixture(params)));

        instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));

        verify(repository, times(1)).find(BUCKET_NAME);
        verify(repository, times(1)).delete(BUCKET_NAME);
        verify(repository, times(1)).save(any(ServiceInstance.class));
        verify(ecs, times(1)).changeBucketPlan(eq(BUCKET_NAME),
                any(ServiceDefinitionProxy.class), any(PlanProxy.class),
                eq(params));
    }

    /**
     * The instance-service cannot change a bucket's plan if it has remote connections
     *
     */
    @Test(expected = ServiceBrokerException.class)
    public void testChangeBucketServiceWithRemoteConnection() throws EcsManagementClientException, IOException, JAXBException {
        Map<String, Object> params = new HashMap<>();
        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                .thenReturn(bucketServiceFixture());
        // Create a service instance with a remote reference
        ServiceInstance inst = new ServiceInstance(bucketCreateRequestFixture(params));
        inst.addReference(SERVICE_INSTANCE_ID);
        when(repository.find(BUCKET_NAME)).thenReturn(inst);

        instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));

        verify(repository, times(1)).find(BUCKET_NAME);
        verify(repository, times(0)).delete(any(String.class));
        verify(repository, times(0)).save(any(ServiceInstance.class));
        verify(ecs, times(0)).changeBucketPlan(any(String.class),
                any(ServiceDefinitionProxy.class), any(PlanProxy.class),
                eq(params));
    }

    /**
     * The instance-service can create a namespace with empty params.
     *
     * @throws EcsManagementClientException
     * @throws IOException
     * @throws JAXBException
     */
    @Test
    public void testCreateNamespaceService()
            throws EcsManagementClientException, IOException, JAXBException {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());

        Map<String, Object> params = new HashMap<>();
        instSvc.createServiceInstance(namespaceCreateRequestFixture(params));

        verify(repository).save(any(ServiceInstance.class));
        verify(ecs, times(1)).createNamespace(eq(NAMESPACE),
                any(ServiceDefinitionProxy.class), any(PlanProxy.class),
                eq(params));
    }

    /**
     * The instance-service can create a namespace with null params.
     *
     * @throws EcsManagementClientException
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateNamespaceServiceNullParams()
            throws EcsManagementClientException, IOException, JAXBException {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());

        instSvc.createServiceInstance(namespaceCreateRequestFixture());

        verify(repository).save(any(ServiceInstance.class));
        verify(ecs, times(1)).createNamespace(eq(NAMESPACE),
                any(ServiceDefinitionProxy.class),
                any(PlanProxy.class), eq(null));
    }

    /**
     * The instance-service can delete a namespace.
     *
     * @throws EcsManagementClientException
     */
    @Test
    public void testDeleteNamespaceService()
            throws EcsManagementClientException, IOException {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        when(repository.find(NAMESPACE))
                .thenReturn(new ServiceInstance(namespaceCreateRequestFixture()));

        instSvc.deleteServiceInstance(namespaceDeleteRequestFixture());

        verify(repository, times(1)).delete(NAMESPACE);
        verify(ecs, times(1)).deleteNamespace(NAMESPACE);
    }

    /**
     * The instance-service can delete a bucket with empty params.
     */
    @Test
    public void testDeleteNamespaceServiceWithRemoteConnection() throws IOException, JAXBException {
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        ServiceInstance inst = new ServiceInstance(namespaceCreateRequestFixture());
        ArgumentCaptor<ServiceInstance> instanceArgumentCaptor = ArgumentCaptor.forClass(ServiceInstance.class);
        inst.addReference(SERVICE_INSTANCE_ID);
        when(repository.find(NAMESPACE)).thenReturn(inst);
        when(repository.find(SERVICE_INSTANCE_ID)).thenReturn(inst);
        doNothing().when(repository).save(instanceArgumentCaptor.capture());

        instSvc.deleteServiceInstance(namespaceDeleteRequestFixture());

        verify(repository, times(0)).delete(SERVICE_INSTANCE_ID);
        verify(repository, times(1)).save(any(ServiceInstance.class));
        verify(repository, times(1)).delete(NAMESPACE);
        verify(ecs, times(0)).deleteBucket(NAMESPACE);

        ServiceInstance reference = instanceArgumentCaptor.getValue();
        assertEquals(1, reference.getReferences().size());
        assertTrue(reference.getReferences().contains(SERVICE_INSTANCE_ID));
    }

    /**
     * The instance-service can change a namespace's plan with empty params.
     *
     * @throws IOException
     * @throws JAXBException
     * @throws EcsManagementClientException
     */
    @Test
    public void testChangeNamespaceService()
            throws IOException, JAXBException, EcsManagementClientException {
        Map<String, Object> params = new HashMap<>();

        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        when(repository.find(NAMESPACE)).thenReturn(
                new ServiceInstance(namespaceCreateRequestFixture(params)));

        instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));

        verify(repository, times(1)).find(NAMESPACE);
        verify(repository, times(1)).delete(NAMESPACE);
        verify(repository, times(1)).save(any(ServiceInstance.class));
        verify(ecs, times(1)).changeNamespacePlan(eq(NAMESPACE),
                any(ServiceDefinitionProxy.class), any(PlanProxy.class), eq(params));
    }

    /**
     * The instance-service cannot change a namespaces's plan if it has remote connections
     *
     */
    @Test(expected = ServiceBrokerException.class)
    public void testChangeNamespaceServiceWithRemoteConnection() throws EcsManagementClientException, IOException, JAXBException {
        Map<String, Object> params = new HashMap<>();
        when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                .thenReturn(namespaceServiceFixture());
        // Create a service instance with a remote reference
        ServiceInstance inst = new ServiceInstance(namespaceCreateRequestFixture(params));
        inst.addReference(SERVICE_INSTANCE_ID);
        when(repository.find(NAMESPACE)).thenReturn(inst);

        instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));

        verify(repository, times(1)).find(NAMESPACE);
        verify(repository, times(0)).delete(any(String.class));
        verify(repository, times(0)).save(any(ServiceInstance.class));
        verify(ecs, times(0)).changeNamespacePlan(any(String.class),
                any(ServiceDefinitionProxy.class), any(PlanProxy.class),
                eq(params));
    }

}