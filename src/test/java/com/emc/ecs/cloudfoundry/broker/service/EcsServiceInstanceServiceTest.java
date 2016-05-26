package com.emc.ecs.cloudfoundry.broker.service;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static com.emc.ecs.common.Fixtures.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import com.emc.ecs.cloudfoundry.broker.service.EcsService;
import com.emc.ecs.cloudfoundry.broker.service.EcsServiceInstanceService;

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
     * 
     */
    @Test
    public void testCreateBucketService() throws EcsManagementClientException,
	    IOException, JAXBException, EcsManagementResourceNotFoundException {
	when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());
	Map<String, Object> params = new HashMap<>();
	instSvc.createServiceInstance(bucketCreateRequestFixture(params));

	verify(repository).save(any(ServiceInstance.class));
	verify(ecs, times(1)).createBucket(eq(BUCKET_NAME),
		any(ServiceDefinitionProxy.class), any(PlanProxy.class));
    }

    /**
     * The instance-service can delete a bucket with empty params.
     * @throws EcsManagementClientException 
     * 
     */
    @Test
    public void testDeleteBucketService() throws EcsManagementClientException {
	when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
		.thenReturn(bucketServiceFixture());

	instSvc.deleteServiceInstance(bucketDeleteRequestFixture());

	verify(repository, times(1)).delete(BUCKET_NAME);
	verify(ecs, times(1)).deleteBucket(BUCKET_NAME);
    }

    /**
     * The instance-service can change a namespace's plan with empty params.
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
		any(ServiceDefinitionProxy.class), any(PlanProxy.class));
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
	verify(ecs, times(1)).createNamespace(eq(NAMESPACE), any(ServiceDefinitionProxy.class),
		any(PlanProxy.class), eq(params));
    }

    /**
     * The instance-service can delete a namespace.
     * 
     * @throws EcsManagementClientException
     */
    @Test
    public void testDeleteNamespaceService()
	    throws EcsManagementClientException {
	when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
		.thenReturn(namespaceServiceFixture());

	instSvc.deleteServiceInstance(namespaceDeleteRequestFixture());

	verify(repository, times(1)).delete(NAMESPACE);
	verify(ecs, times(1)).deleteNamespace(NAMESPACE);
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
}