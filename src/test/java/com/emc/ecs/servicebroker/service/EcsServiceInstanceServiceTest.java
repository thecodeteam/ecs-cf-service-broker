package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.model.ServiceType;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.servicebroker.model.Constants.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(Ginkgo4jRunner.class)
public class EcsServiceInstanceServiceTest {

    private EcsServiceInstanceService instSvc;
    private ServiceDefinitionProxy serviceDef;
    private PlanProxy plan;
    private CreateServiceInstanceRequest createReq;
    private Map<String, Object> settings;

    private final EcsService ecs = mock(EcsService.class);
    private final Map<String, Object> params = new HashMap<>();
    private final ServiceInstanceRepository repo = mock(ServiceInstanceRepository.class);

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
                            when(ecs.createBucket(BUCKET_NAME, BUCKET_NAME, serviceDef, plan, params))
                                    .thenReturn(settings);
                            instSvc.createServiceInstance(createReq);
                        });

                        It("should create the bucket", () ->
                                verify(ecs, times(1))
                                        .createBucket(BUCKET_NAME, BUCKET_NAME,  serviceDef, plan, params));

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
                            Map quota = (Map) instCap.getValue().getServiceSettings().get(QUOTA);
                            assertEquals(4, quota.get(QUOTA_WARN));
                            assertEquals(5, quota.get(QUOTA_LIMIT));
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
                                    assert (e.getMessage().contains("invalid accessKey / secretKey combination"));
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
                                    .createBucket(any(), any(), any(), any(), any()));

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
                            ServiceInstance inst = new ServiceInstance(bucketCreateRequestFixture(params));
                            when(repo.find(BUCKET_NAME)).thenReturn(inst);
                            when(ecs.getDefaultNamespace()).thenReturn(NAMESPACE_NAME);

                            instSvc.deleteServiceInstance(bucketDeleteRequestFixture());
                        });

                        It("should delete the bucket", () ->
                                verify(ecs, times(1))
                                        .deleteBucket(BUCKET_NAME, NAMESPACE_NAME));

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
                                        .deleteBucket(any(),any()));

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
                            when(ecs.changeBucketPlan(BUCKET_NAME, serviceDef, plan, params, null))
                                    .thenReturn(settings);
                            instSvc.updateServiceInstance(bucketUpdateRequestFixture(params));
                        });

                        It("should find the service instance in the repo", () ->
                                verify(repo, times(2))
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
                                        .changeBucketPlan(BUCKET_NAME, serviceDef, plan, params, null));
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
                            Map quota = (Map) instCap.getValue().getServiceSettings().get(QUOTA);
                            assertEquals(4, quota.get(QUOTA_WARN));
                            assertEquals(5, quota.get(QUOTA_LIMIT));
                        });

                    });

                    Context("with null params", () -> {
                        BeforeEach(() -> {
                            when(ecs.createNamespace(NAMESPACE_NAME, serviceDef, plan, Collections.emptyMap()))
                                    .thenReturn(settings);
                            instSvc.createServiceInstance(namespaceCreateRequestFixture());
                        });

                        It("should create the namespace", () ->
                                verify(ecs, times(1))
                                        .createNamespace(NAMESPACE_NAME, serviceDef, plan, Collections.emptyMap()));


                        It("should save the instance to the repository", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(NAMESPACE_NAME, instCap.getValue().getServiceInstanceId());
                        });

                        It("should save the service settings to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            Map quota = (Map) instCap.getValue().getServiceSettings().get(QUOTA);
                            assertEquals(4, quota.get(QUOTA_WARN));
                            assertEquals(5, quota.get(QUOTA_LIMIT));
                        });

                    });

                    Context("remote service", () -> {
                        BeforeEach(() -> {
                            ServiceInstance repoInst =
                                    new ServiceInstance(remoteNamespaceCreateRequestFixture(params));
                            repoInst.addRemoteConnectionKey(BINDING_ID, REMOTE_CONNECT_KEY);
                            repoInst.setServiceSettings(settings);
                            when(repo.find(NAMESPACE_NAME))
                                    .thenReturn(repoInst);
                        });

                        Context("without valid remote connect creds", () -> {

                            BeforeEach(() -> {
                                params.put(REMOTE_CONNECTION, remoteConnect(NAMESPACE_NAME, "junk"));
                                createReq = remoteNamespaceCreateRequestFixture(params);
                            });

                            It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                                try {
                                    instSvc.createServiceInstance(createReq);
                                } catch (ServiceBrokerException e) {
                                    assert (e.getMessage().contains("invalid accessKey / secretKey combination"));
                                }
                            });

                        });

                        Context("with valid remote connect creds", () -> {

                            BeforeEach(() -> {
                                params.put(REMOTE_CONNECTION, remoteConnect(NAMESPACE_NAME, REMOTE_CONNECT_KEY));
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
                                assertEquals(NAMESPACE_NAME, remoteInst.getServiceInstanceId());

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
                                    new ServiceInstance(namespaceCreateRequestFixture(SERVICE_INSTANCE_ID, params));
                            when(repo.find(SERVICE_INSTANCE_ID))
                                    .thenReturn(inst);
                            instSvc.deleteServiceInstance(namespaceDeleteRequestFixture(inst.getServiceInstanceId()));
                        });

                        It("should delete the namespace", () ->
                                verify(ecs, times(1))
                                        .deleteNamespace(SERVICE_INSTANCE_ID));
                    });

                    Context(WITH_REMOTE_CONNECTION, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(namespaceCreateRequestFixture(params));
                            inst.addReference(NAMESPACE_NAME);
                            when(repo.find(NAMESPACE_NAME))
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
                                        .delete(NAMESPACE_NAME));

                        It("should not delete the remote instance from the repo", () ->
                                verify(repo, times(0))
                                        .delete(SERVICE_INSTANCE_ID));

                    });
                });

                Context("#updateServiceInstance", () -> {
                    Context(BASIC_SERVICE, () -> {
                        BeforeEach(() -> {
                            when(repo.find(SERVICE_INSTANCE_ID))
                                    .thenReturn(new ServiceInstance(namespaceCreateRequestFixture(params)));
                            when(ecs.changeNamespacePlan(SERVICE_INSTANCE_ID, serviceDef, plan, params))
                                    .thenReturn(settings);
                            instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));
                        });

                        It("should find the service instance in the repo", () ->
                                verify(repo, times(2))
                                        .find(SERVICE_INSTANCE_ID));

                        It("should not delete the service instance from the repo", () ->
                                verify(repo, times(0))
                                        .delete(SERVICE_INSTANCE_ID));

                        It("should save the updated service instance to the repo", () -> {
                            ArgumentCaptor<ServiceInstance> instCap =
                                    ArgumentCaptor.forClass(ServiceInstance.class);
                            verify(repo, times(1))
                                    .save(instCap.capture());
                            assertEquals(SERVICE_INSTANCE_ID, instCap.getValue().getServiceInstanceId());
                            assertEquals(ServiceType.NAMESPACE.getAlias(),
                                    instCap.getValue().getServiceSettings().get(SERVICE_TYPE));
                        });

                        It("should update the namespace", () ->
                                verify(ecs, times(1))
                                        .changeNamespacePlan(SERVICE_INSTANCE_ID, serviceDef, plan, params));

                    });

                    Context(WITH_REMOTE_CONNECTION, () -> {
                        BeforeEach(() -> {
                            ServiceInstance inst =
                                    new ServiceInstance(namespaceCreateRequestFixture(params));
                            inst.addReference(SERVICE_INSTANCE_ID);
                            when(repo.find(SERVICE_INSTANCE_ID))
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
                            when(repo.find(SERVICE_INSTANCE_ID))
                                    .thenReturn(inst);
                        });

                        It(SHOULD_RAISE_AN_EXCEPTION, () -> {
                            try {
                                params.put(REMOTE_CONNECTION, remoteConnect(NAMESPACE_NAME, REMOTE_CONNECT_KEY));
                                instSvc.updateServiceInstance(namespaceUpdateRequestFixture(params));
                            } catch (ServiceBrokerException e) {
                                assert (e.getMessage().endsWith("remote_connection parameter invalid for plan upgrade"));
                            }
                        });

                    });

                    Context("with missing service instance", () -> {
                        BeforeEach(() ->
                                when(repo.find(NAMESPACE_NAME))
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
        remoteConnection.put(CREDENTIALS_ACCESS_KEY, BINDING_ID);
        remoteConnection.put(CREDENTIALS_SECRET_KEY, secretKey);
        remoteConnection.put(CREDENTIALS_INSTANCE_ID, instanceId);
        return remoteConnection;
    }

}
