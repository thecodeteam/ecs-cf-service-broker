package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;

import java.util.HashMap;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(Ginkgo4jRunner.class)
public class EcsServiceInstanceServiceTest {

    private EcsService ecs = mock(EcsService.class);
    private ServiceInstanceRepository repo = mock(ServiceInstanceRepository.class);
    private EcsServiceInstanceService instSvc;
    private ServiceDefinitionProxy serviceDef;
    private PlanProxy plan;
    private Map<String, Object> params = new HashMap<>();
    private CreateServiceInstanceRequest createReq;
    private Map<String, Object> settings;

    public static final String BASIC_SERVICE = "basic service";

    public static final String WITH_REMOTE_CONNECTION = "with remote connection";

    {
        Describe("EcsServiceInstanceService", () -> {
            BeforeEach(() -> instSvc = new EcsServiceInstanceService(ecs, repo));

            Context("Bucket Service", () -> {
                BeforeEach(() -> {
                    serviceDef = bucketServiceFixture();
                    plan = serviceDef.getPlans().get(0);
                    settings = resolveSettings(serviceDef, plan, params);
                    when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                            .thenReturn(serviceDef);
                });

                Context("#createServiceInstance", () -> {
                    BeforeEach(() -> {
                    });
                    Context(BASIC_SERVICE, () -> {

                        BeforeEach(() -> {
                            createReq = bucketCreateRequestFixture(params);
                            when(ecs.createBucket(BUCKET_NAME, serviceDef, plan, params))
                                    .thenReturn(settings);
                            instSvc.createServiceInstance(createReq);
                        });

                        It("should create the bucket", () ->
                                verify(ecs, times(1))
                                        .createBucket(BUCKET_NAME, serviceDef, plan, params));

                        It("should save the service instance to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(BUCKET_NAME, instCap.getValue().getServiceInstanceId());
                        });

                        It("should save the service settings to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            Map quota = (Map) instCap.getValue().getServiceSettings().get("quota");
                            assertEquals(4, quota.get("warn"));
                            assertEquals(5, quota.get("limit"));
                        });

                    });

                    Context("remote service", () -> {
                        BeforeEach(() -> {
                            ServiceInstance repoInst =
                                    new ServiceInstance(bucketCreateRequestFixture(params));
                            repoInst.addRemoteConnectionKey(BINDING_ID, REMOTE_CONNECT_KEY);
                            repoInst.setServiceSettings(settings);
                            when(repo.find(BUCKET_NAME))
                                    .thenReturn(repoInst);
                        });

                        Context("without valid remote connect creds", () -> {

                            BeforeEach(() -> {
                                params.put(REMOTE_CONNECTION, remoteConnect(BUCKET_NAME, "junk"));
                                createReq = remoteBucketCreateRequestFixture(params);
                            });

                            It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                                try {
                                    instSvc.createServiceInstance(createReq);
                                } catch (ServiceBrokerException e) {
                                    assert (e.getMessage().endsWith("invalid accessKey / secretKey combination"));
                                }
                            });

                        });

                        Context("with valid remote connect creds", () -> {

                            BeforeEach(() -> {
                                params.put(REMOTE_CONNECTION, remoteConnect(BUCKET_NAME, REMOTE_CONNECT_KEY));
                                createReq = remoteBucketCreateRequestFixture(params);
                                instSvc.createServiceInstance(createReq);
                            });

                            It("should not create a bucket", () -> verify(ecs, times(0))
                                    .createBucket(any(), any(), any(), any()));

                            It("should save the original & remote service instances to the repo", () -> {
                                ArgumentCaptor<ServiceInstance> instCap =
                                        ArgumentCaptor.forClass(ServiceInstance.class);
                                verify(repo, times(2))
                                        .save(instCap.capture());
                                ServiceInstance remoteInst = instCap.getAllValues().get(0);
                                assertEquals(BUCKET_NAME, remoteInst.getServiceInstanceId());

                                ServiceInstance localInst = instCap.getAllValues().get(1);
                                assertEquals(SERVICE_INSTANCE_ID, localInst.getServiceInstanceId());
                            });

                            It("should update the remote references", () -> {
                                ArgumentCaptor<ServiceInstance> instCap =
                                        ArgumentCaptor.forClass(ServiceInstance.class);
                                verify(repo, times(2))
                                        .save(instCap.capture());
                                ServiceInstance remoteInst = instCap.getAllValues().get(0);
                                assertEquals(2, remoteInst.getReferenceCount());
                            });

                            It("should update the local references", () -> {
                                ArgumentCaptor<ServiceInstance> instCap =
                                        ArgumentCaptor.forClass(ServiceInstance.class);
                                verify(repo, times(2))
                                        .save(instCap.capture());
                                ServiceInstance localInst = instCap.getAllValues().get(1);
                                assertEquals(2, localInst.getReferenceCount());
                            });

                        });
                    });
                });

                Context("#deleteServiceInstnace", () -> {
                    Context(BASIC_SERVICE, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(bucketCreateRequestFixture(params));
                            when(repo.find(BUCKET_NAME))
                                    .thenReturn(inst);
                            instSvc.deleteServiceInstance(bucketDeleteRequestFixture());
                        });

                        It("should delete the bucket", () ->
                                verify(ecs, times(1))
                                        .deleteBucket(BUCKET_NAME));

                        It("should delete the instance from the repository", () ->
                                verify(repo, times(1))
                                        .delete(BUCKET_NAME));
                    });

                    Context(WITH_REMOTE_CONNECTION, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(bucketCreateRequestFixture(params));
                            inst.addReference(SERVICE_INSTANCE_ID);
                            when(repo.find(BUCKET_NAME))
                                    .thenReturn(inst);
                            when(repo.find(SERVICE_INSTANCE_ID))
                                    .thenReturn(inst);
                            instSvc.deleteServiceInstance(bucketDeleteRequestFixture());
                        });

                        It("should not delete the bucket", () ->
                                verify(ecs, times(0))
                                        .deleteBucket(any()));

                        It("should save an update of the remote instance in the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap = ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(BUCKET_NAME, instCap.getValue().getServiceInstanceId());
                        });

                        It("should still have the remote instance in the references", () -> {
                            ArgumentCaptor<ServiceInstance> instCap = ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assert (instCap.getValue().getReferences().contains(SERVICE_INSTANCE_ID));
                        });

                        It("should delete this instance from the repo", () ->
                                verify(repo, times(1))
                                        .delete(eq(BUCKET_NAME)));

                        It("should not delete the remote instance from the repo", () ->
                                verify(repo, times(0))
                                        .delete(eq(SERVICE_INSTANCE_ID)));
                    });

                });

                Context("#updateServiceInstance", () -> {
                    Context(BASIC_SERVICE, () -> {
                        BeforeEach(() -> {
                            when(repo.find(BUCKET_NAME))
                                    .thenReturn(new ServiceInstance(bucketCreateRequestFixture(params)));
                            when(ecs.changeBucketPlan(BUCKET_NAME, serviceDef, plan, params))
                                    .thenReturn(settings);
                            instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));
                        });

                        It("should find the service instance in the repo", () ->
                                verify(repo, times(1))
                                        .find(BUCKET_NAME));

                        It("should not delete the service instance from the repo", () ->
                                verify(repo, times(0))
                                        .delete(BUCKET_NAME));

                        It("should save the updated service instance to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(BUCKET_NAME, instCap.getValue().getServiceInstanceId());
                            assertEquals("bucket",
                                    instCap.getValue().getServiceSettings().get("service-type"));
                        });

                        It("should update the bucket", () ->
                                verify(ecs, times(1))
                                        .changeBucketPlan(BUCKET_NAME, serviceDef, plan, params));
                    });

                    Context(WITH_REMOTE_CONNECTION, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst = new ServiceInstance(bucketCreateRequestFixture(params));
                            inst.addReference(SERVICE_INSTANCE_ID);
                            when(repo.find(BUCKET_NAME))
                                    .thenReturn(inst);
                        });

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));
                            } catch (ServiceBrokerException e) {
                                assert (e.getMessage()
                                        .endsWith("Cannot change plan of service instance with remote references"));
                            }
                        });

                    });

                    Context("with missing service instance", () -> {
                        BeforeEach(() ->
                                when(repo.find(BUCKET_NAME))
                                        .thenReturn(null));

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));
                            } catch (ServiceInstanceDoesNotExistException e) {
                                assertEquals(ServiceInstanceDoesNotExistException.class, e.getClass());
                            }
                        });
                    });

                    Context("with remote connection parameters", () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst = new ServiceInstance(bucketCreateRequestFixture(params));
                            when(repo.find(BUCKET_NAME))
                                    .thenReturn(inst);
                        });

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                params.put(REMOTE_CONNECTION, remoteConnect(BUCKET_NAME, REMOTE_CONNECT_KEY));
                                instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));
                            } catch (ServiceBrokerException e) {
                                assert (e.getMessage().endsWith("remote_connection parameter invalid for plan upgrade"));
                            }
                        });
                    });
                });
            });

            Context("Namespace Service", () -> {

                BeforeEach(() -> {
                    serviceDef = namespaceServiceFixture();
                    plan = serviceDef.getPlans().get(0);
                    settings = resolveSettings(serviceDef, plan, params);
                    when(ecs.lookupServiceDefinition(NAMESPACE_SERVICE_ID))
                            .thenReturn(serviceDef);
                });

                Context("#createServiceInstance", () -> {
                    Context(BASIC_SERVICE, () -> {
                        BeforeEach(() -> {
                            when(ecs.createNamespace(SERVICE_INSTANCE_ID, serviceDef, plan, params))
                                    .thenReturn(settings);
                            instSvc.createServiceInstance(namespaceCreateRequestFixture(params));
                        });

                        It("should create the namespace", () ->
                                verify(ecs, times(1))
                                        .createNamespace(SERVICE_INSTANCE_ID, serviceDef, plan, params));

                        It("should save the instance to the repository", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(SERVICE_INSTANCE_ID, instCap.getValue().getServiceInstanceId());
                        });

                        It("should save the service settings to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            Map quota = (Map) instCap.getValue().getServiceSettings().get("quota");
                            assertEquals(4, quota.get("warn"));
                            assertEquals(5, quota.get("limit"));
                        });

                    });

                    Context("with null params", () -> {
                        BeforeEach(() -> {
                            when(ecs.createNamespace(NAMESPACE, serviceDef, plan, null))
                                    .thenReturn(settings);
                            instSvc.createServiceInstance(namespaceCreateRequestFixture());
                        });

                        It("should create the namespace", () ->
                                verify(ecs, times(1))
                                        .createNamespace(NAMESPACE, serviceDef, plan, null));


                        It("should save the instance to the repository", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(NAMESPACE, instCap.getValue().getServiceInstanceId());
                        });

                        It("should save the service settings to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            Map quota = (Map) instCap.getValue().getServiceSettings().get("quota");
                            assertEquals(4, quota.get("warn"));
                            assertEquals(5, quota.get("limit"));
                        });

                    });

                    Context("remote service", () -> {
                        BeforeEach(() -> {
                            ServiceInstance repoInst =
                                    new ServiceInstance(remoteNamespaceCreateRequestFixture(params));
                            repoInst.addRemoteConnectionKey(BINDING_ID, REMOTE_CONNECT_KEY);
                            repoInst.setServiceSettings(settings);
                            when(repo.find(NAMESPACE))
                                    .thenReturn(repoInst);
                        });

                        Context("without valid remote connect creds", () -> {

                            BeforeEach(() -> {
                                params.put(REMOTE_CONNECTION, remoteConnect(NAMESPACE, "junk"));
                                createReq = remoteNamespaceCreateRequestFixture(params);
                            });

                            It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                                try {
                                    instSvc.createServiceInstance(createReq);
                                } catch (ServiceBrokerException e) {
                                    assert (e.getMessage().endsWith("invalid accessKey / secretKey combination"));
                                }
                            });

                        });

                        Context("with valid remote connect creds", () -> {

                            BeforeEach(() -> {
                                params.put(REMOTE_CONNECTION, remoteConnect(NAMESPACE, REMOTE_CONNECT_KEY));
                                createReq = namespaceCreateRequestFixture(params);
                                instSvc.createServiceInstance(createReq);
                            });

                            It("should not create a namespace", ()
                                    -> verify(ecs, times(0))
                                    .createNamespace(any(), any(), any(), any()));

                            It("should save the original & remote service instances to the repo", () -> {
                                ArgumentCaptor<ServiceInstance> instCap =
                                        ArgumentCaptor.forClass(ServiceInstance.class);
                                verify(repo, times(2))
                                        .save(instCap.capture());
                                ServiceInstance remoteInst = instCap.getAllValues().get(0);
                                assertEquals(NAMESPACE, remoteInst.getServiceInstanceId());

                                ServiceInstance localInst = instCap.getAllValues().get(1);
                                assertEquals(SERVICE_INSTANCE_ID, localInst.getServiceInstanceId());
                            });

                            It("should update the remote references", () -> {
                                ArgumentCaptor<ServiceInstance> instCap =
                                        ArgumentCaptor.forClass(ServiceInstance.class);
                                verify(repo, times(2))
                                        .save(instCap.capture());
                                ServiceInstance remoteInst = instCap.getAllValues().get(0);
                                assertEquals(2, remoteInst.getReferenceCount());
                            });

                            It("should update the local references", () -> {
                                ArgumentCaptor<ServiceInstance> instCap =
                                        ArgumentCaptor.forClass(ServiceInstance.class);
                                verify(repo, times(2))
                                        .save(instCap.capture());
                                ServiceInstance localInst = instCap.getAllValues().get(1);
                                assertEquals(2, localInst.getReferenceCount());
                            });

                        });
                    });
                });

                Context("#deleteServiceInstance", () -> {
                    Context(BASIC_SERVICE, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(namespaceCreateRequestFixture(params));
                            when(repo.find(NAMESPACE))
                                    .thenReturn(inst);
                            instSvc.deleteServiceInstance(namespaceDeleteRequestFixture());
                        });

                        It("should delete the namespace", () ->
                                verify(ecs, times(1))
                                        .deleteNamespace(NAMESPACE));
                    });

                    Context(WITH_REMOTE_CONNECTION, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(namespaceCreateRequestFixture(params));
                            inst.addReference(NAMESPACE);
                            when(repo.find(NAMESPACE))
                                    .thenReturn(inst);
                            when(repo.find(SERVICE_INSTANCE_ID))
                                    .thenReturn(inst);
                            instSvc.deleteServiceInstance(namespaceDeleteRequestFixture());
                        });

                        It("should not delete the namespace", () ->
                                verify(ecs, times(0))
                                        .deleteNamespace(any()));

                        It("should save an update of the remote instance in the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(SERVICE_INSTANCE_ID, instCap.getValue().getServiceInstanceId());
                        });

                        It("should still have the remote instance in the references", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assert (instCap.getValue().getReferences().contains(SERVICE_INSTANCE_ID));
                        });

                        It("should delete this instance from the repo", () ->
                                verify(repo, times(1))
                                        .delete(NAMESPACE));

                        It("should not delete the remote instance from the repo", () ->
                                verify(repo, times(0))
                                        .delete(SERVICE_INSTANCE_ID));

                    });
                });

                Context("#updateServiceInstance", () -> {
                    Context(BASIC_SERVICE, () -> {
                        BeforeEach(() -> {
                            when(repo.find(NAMESPACE))
                                    .thenReturn(new ServiceInstance(namespaceCreateRequestFixture(params)));
                            when(ecs.changeNamespacePlan(NAMESPACE, serviceDef, plan, params))
                                    .thenReturn(settings);
                            instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));
                        });

                        It("should find the service instance in the repo", () ->
                                verify(repo, times(1))
                                        .find(NAMESPACE));

                        It("should not delete the service instance from the repo", () ->
                                verify(repo, times(0))
                                        .delete(NAMESPACE));

                        It("should save the updated service instance to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(NAMESPACE, instCap.getValue().getServiceInstanceId());
                            assertEquals("namespace",
                                    instCap.getValue().getServiceSettings().get("service-type"));
                        });

                        It("should update the namespace", () ->
                                verify(ecs, times(1))
                                        .changeNamespacePlan(NAMESPACE, serviceDef, plan, params));

                    });

                    Context(WITH_REMOTE_CONNECTION, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(namespaceCreateRequestFixture(params));
                            inst.addReference(SERVICE_INSTANCE_ID);
                            when(repo.find(NAMESPACE))
                                    .thenReturn(inst);
                        });

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));
                            } catch (ServiceBrokerException e) {
                                assert (e.getMessage()
                                        .endsWith("Cannot change plan of service instance with remote references"));
                            }
                        });
                    });

                    Context("with remote connection parameters", () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst = new ServiceInstance(namespaceCreateRequestFixture(params));
                            when(repo.find(NAMESPACE))
                                    .thenReturn(inst);
                        });

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                params.put(REMOTE_CONNECTION, remoteConnect(NAMESPACE, REMOTE_CONNECT_KEY));
                                instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));
                            } catch (ServiceBrokerException e) {
                                assert (e.getMessage().endsWith("remote_connection parameter invalid for plan upgrade"));
                            }
                        });

                    });

                    Context("with missing service instance", () -> {
                        BeforeEach(() ->
                                when(repo.find(NAMESPACE))
                                        .thenReturn(null));

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));
                            } catch (ServiceInstanceDoesNotExistException e) {
                                assertEquals(ServiceInstanceDoesNotExistException.class, e.getClass());
                            }
                        });
                    });
                });
            });


        });
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

    private Map<String, String> remoteConnect(String instanceId, String secretKey) {
        Map<String, String> remoteConnection = new HashMap<>();
        remoteConnection.put("accessKey", BINDING_ID);
        remoteConnection.put("secretKey", secretKey);
        remoteConnection.put("instanceId", instanceId);
        return remoteConnection;
    }

}
