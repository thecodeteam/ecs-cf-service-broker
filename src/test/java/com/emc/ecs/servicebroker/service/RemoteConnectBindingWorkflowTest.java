package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.workflow.InstanceWorkflow;
import com.emc.ecs.servicebroker.workflow.RemoteConnectionInstanceWorkflow;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;

import java.util.HashMap;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.servicebroker.model.Constants.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.It;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(Ginkgo4jRunner.class)
public class RemoteConnectBindingWorkflowTest {
    private EcsService ecs;
    private ServiceInstanceRepository instanceRepo;
    private Map<String, Object> params = new HashMap<>();
    private InstanceWorkflow workflow;
    private ServiceDefinitionProxy serviceProxy = new ServiceDefinitionProxy();
    private PlanProxy planProxy = new PlanProxy();
    private ArgumentCaptor<ServiceInstance> instCap = ArgumentCaptor.forClass(ServiceInstance.class);
    private ServiceInstance localInst;
    private Map<String, Object> settings;

    {
        serviceProxy.getServiceSettings().put(SERVICE_TYPE, ServiceType.BUCKET.name());
        serviceProxy.getServiceSettings().put(REPLICATION_GROUP, "someRG");
        planProxy.getServiceSettings().put(ENCRYPTED, false);
    }

    {
        Describe("RemoteConnectionBindingWorkflow", () -> {
            BeforeEach(() -> {
                ecs = mock(EcsService.class);
                instanceRepo = mock(ServiceInstanceRepository.class);
                workflow = new RemoteConnectionInstanceWorkflow(instanceRepo, ecs);
            });

            Context("#createBinding", () -> {

                BeforeEach(() -> {
                    params.put(REMOTE_CONNECTION, remoteConnect(BUCKET_NAME, REMOTE_CONNECT_KEY));
                    CreateServiceInstanceRequest createReq = bucketCreateRequestFixture(params);
                    createReq.setServiceInstanceId(SERVICE_INSTANCE_ID);
                    workflow.withCreateRequest(createReq);

                    // TODO create binding here
                });

                Context("when remote instance doesn't exist", () -> {

                    BeforeEach(() ->
                            when(instanceRepo.find(BUCKET_NAME))
                                    .thenReturn(null));

                    It("should raise an exception", () -> {
                        try {
                            workflow.create(SERVICE_INSTANCE_ID, serviceProxy, planProxy, params);
                        } catch (ServiceBrokerException e) {
                            String message = "Remotely connected service instance not found";
                            assertEquals(ServiceBrokerException.class, e.getClass());
                            assertEquals(message, e.getMessage());
                        }
                    });


                });

                Context("with valid remote connect credentials", () -> {
                    BeforeEach(() -> {
                        settings = resolveSettings(serviceProxy, planProxy, new HashMap<>());
                        ServiceInstance remoteInst = new ServiceInstance(bucketCreateRequestFixture(params));
                        remoteInst.addRemoteConnectionKey(BINDING_ID, REMOTE_CONNECT_KEY);
                        remoteInst.setServiceSettings(settings);
                        when(instanceRepo.find(BUCKET_NAME))
                                .thenReturn(remoteInst);
                    });

                    Context("when service definitions don't match", () ->
                            It("should raise an exception", () -> {
                                try {
                                    Map<String, Object> newParams = new HashMap<>(params);
                                    newParams.put(ENCRYPTED, true);
                                    workflow.create(SERVICE_INSTANCE_ID, serviceProxy, planProxy, newParams);
                                } catch (ServiceBrokerException e) {
                                    String message = "service definition must match between local and remote instances";
                                    assertEquals(ServiceBrokerException.class, e.getClass());
                                    assertEquals(message, e.getMessage());
                                }
                            })
                    );

                    Context("when service definitions match", () -> {
                        BeforeEach(() -> {
                            localInst = workflow.create(SERVICE_INSTANCE_ID, serviceProxy, planProxy, params);
                            instCap = ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(instanceRepo, times(1))
                                    .save(instCap.capture());
                        });

                        It("should find the remote instance", () ->
                                verify(instanceRepo, times(1))
                                        .find(BUCKET_NAME));

                        It("should save the the remote service instance", () ->
                                assertEquals(BUCKET_NAME,
                                        instCap.getValue().getServiceInstanceId()));

                        It("should save the remote references", () -> {
                            ServiceInstance remoteInst = instCap.getValue();
                            assertEquals(2, remoteInst.getReferenceCount());
                            assert (remoteInst.getReferences().contains(BUCKET_NAME));
                            assert (remoteInst.getReferences().contains(SERVICE_INSTANCE_ID));
                        });

                        It("should save the remote service settings", () ->
                        {
                            Map<String, Object> remoteInstanceSettings = instCap.getValue().getServiceSettings();
                            assertEquals(settings, remoteInstanceSettings);
                        });

                        It("should return the local instance", () ->
                                assertEquals(SERVICE_INSTANCE_ID,
                                        localInst.getServiceInstanceId()));

                        It("should save the local references", () -> {
                            assertEquals(2, instCap.getValue().getReferenceCount());
                            assert (localInst.getReferences().contains(BUCKET_NAME));
                            assert (localInst.getReferences().contains(SERVICE_INSTANCE_ID));
                        });

                        It("should not yet have service settings", () ->
                                assertNull(localInst.getServiceSettings()));

                    });

                });

            });

        });

    }

    private Map<String, String> remoteConnect(String instanceId, String secretKey) {
        Map<String, String> remoteConnection = new HashMap<>();
        remoteConnection.put(CREDENTIALS_ACCESS_KEY, BINDING_ID);
        remoteConnection.put(CREDENTIALS_SECRET_KEY, secretKey);
        remoteConnection.put(CREDENTIALS_INSTANCE_ID, instanceId);
        return remoteConnection;
    }

    private Map<String, Object> resolveSettings(
            ServiceDefinitionProxy service,
            PlanProxy plan,
            Map<String, Object> params
    ) {
        params.putAll(plan.getServiceSettings());
        params.putAll(service.getServiceSettings());
        return params;
    }

}
