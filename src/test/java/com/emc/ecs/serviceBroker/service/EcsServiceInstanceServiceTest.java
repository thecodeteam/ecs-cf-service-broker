package com.emc.ecs.serviceBroker.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.ecs.serviceBroker.config.BrokerConfig;
import com.emc.ecs.serviceBroker.config.CatalogConfig;
import com.emc.ecs.serviceBroker.model.PlanProxy;
import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxy;
import com.emc.ecs.serviceBroker.repository.ServiceInstance;
import com.emc.ecs.serviceBroker.repository.ServiceInstanceRepository;

@RunWith(MockitoJUnitRunner.class)
public class EcsServiceInstanceServiceTest {

    private static final String NAMESPACE = "ns1";
    private static final String SERVICE_ID = "09cac1c6-1b0a-11e6-b6ba-3e1d05defe78";
    private static final String PLAN_ID = "09cac5b8-1b0a-11e6-b6ba-3e1d05defe78";
    private static final String ORG_ID = "55083e67-f841-4c7e-9a19-2bf4d0cac6b9";
    private static final String SPACE_ID = "305c3c4d-ca6c-435d-b77f-046f8bc70e79";

    @Mock
    private EcsService ecs;

    @Mock
    private ServiceInstanceRepository repository;

    @Mock
    private BrokerConfig broker;

    @Mock
    private CatalogConfig catalog;

    @Autowired
    @InjectMocks
    EcsServiceInstanceService instanceService;

    @Test
    public void testCreateNamespaceService()
	    throws EcsManagementClientException, IOException, JAXBException {
	when(catalog.findServiceDefinition(SERVICE_ID))
		.thenReturn(namespaceServiceFixture());

	when(ecs.namespaceExists(NAMESPACE)).thenReturn(false).thenReturn(true);

	Map<String, Object> params = new HashMap<>();
	instanceService.createServiceInstance(
		createRequestFixture(params));

	verify(repository).save(any(ServiceInstance.class));
	verify(ecs, times(2)).namespaceExists(NAMESPACE);
	verify(ecs, times(1)).createNamespace(NAMESPACE, SERVICE_ID, PLAN_ID,
		params);
    }

    @Test
    public void testDeleteNamespaceService()
	    throws EcsManagementClientException {
	when(catalog.findServiceDefinition(SERVICE_ID))
		.thenReturn(namespaceServiceFixture());

	instanceService
		.deleteServiceInstance(deleteRequestFixture());

	verify(repository, times(1)).delete(NAMESPACE);
	verify(ecs, times(1)).deleteNamespace(NAMESPACE);
    }

    @Test
    public void testChangeNamespaceService()
	    throws IOException, JAXBException, EcsManagementClientException {
	Map<String, Object> params = new HashMap<>();

	when(catalog.findServiceDefinition(SERVICE_ID))
		.thenReturn(namespaceServiceFixture());
	when(repository.find(NAMESPACE))
		.thenReturn(new ServiceInstance(createRequestFixture(params)));

	instanceService.updateServiceInstance(
		updateRequestFixture(params));

	verify(repository, times(1)).find(NAMESPACE);
	verify(repository, times(1)).delete(NAMESPACE);
	verify(repository, times(1)).save(any(ServiceInstance.class));
	verify(ecs, times(1)).changeNamespacePlan(NAMESPACE, SERVICE_ID,
		PLAN_ID, params);
    }

    private ServiceDefinitionProxy namespaceServiceFixture() {
	PlanProxy namespacePlan = new PlanProxy(PLAN_ID, "5gb", "Free Trial",
		null, true);
	List<PlanProxy> plans = Arrays.asList(namespacePlan);
	List<String> tags = Arrays.asList("ecs-namespace", "object");
	Map<String, Object> serviceSettings = new HashMap<>();
	serviceSettings.put("service-type", "namespace");
	ServiceDefinitionProxy namespaceService = new ServiceDefinitionProxy(
		SERVICE_ID, "ecs-namespace", "ECS Namespace", true, true, tags,
		serviceSettings, null, plans, null, null);
	return namespaceService;
    }

    private CreateServiceInstanceRequest createRequestFixture(
	    Map<String, Object> params) {
	return new CreateServiceInstanceRequest(SERVICE_ID, PLAN_ID, ORG_ID,
		SPACE_ID, params).withServiceInstanceId(NAMESPACE);
    }

    private UpdateServiceInstanceRequest updateRequestFixture(
	    Map<String, Object> params) {
	return new UpdateServiceInstanceRequest(SERVICE_ID, PLAN_ID, params)
		.withServiceInstanceId(NAMESPACE);
    }

    private DeleteServiceInstanceRequest deleteRequestFixture() {
	return new DeleteServiceInstanceRequest(NAMESPACE, SERVICE_ID, PLAN_ID,
		null);
    }
}