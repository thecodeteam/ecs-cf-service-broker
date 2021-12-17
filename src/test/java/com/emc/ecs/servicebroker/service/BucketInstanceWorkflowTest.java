package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ReclaimPolicy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.emc.ecs.common.Fixtures.*;
import static com.emc.ecs.servicebroker.model.Constants.RECLAIM_POLICY;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Ginkgo4jRunner.class)
public class BucketInstanceWorkflowTest {

    private EcsService ecs;
    private ServiceInstanceRepository instanceRepo;
    private InstanceWorkflow workflow;
    private final Map<String, Object> parameters = new HashMap<>();
    private final ServiceDefinitionProxy serviceProxy = new ServiceDefinitionProxy();
    private final PlanProxy planProxy = new PlanProxy();
    private final ServiceInstance bucketInstance = serviceInstanceFixture();
    private final ServiceInstance namedBucketInstance = serviceInstanceWithNameFixture(BUCKET_NAME);
    private final ArgumentCaptor<ServiceInstance> instCaptor = ArgumentCaptor.forClass(ServiceInstance.class);

    {
        Describe("BucketInstanceWorkflow", () -> {
            BeforeEach(() -> {
                ecs = mock(EcsService.class);
                instanceRepo = mock(ServiceInstanceRepository.class);
                workflow = new BucketInstanceWorkflow(instanceRepo, ecs);

                when(ecs.wipeAndDeleteBucket(any(), any())).thenReturn(CompletableFuture.completedFuture(true));
                when(ecs.getDefaultNamespace()).thenReturn(NAMESPACE_NAME);
            });

            Context("#changePlan", () -> {
                BeforeEach(() -> {
                    when(instanceRepo.find(SERVICE_INSTANCE_ID))
                        .thenReturn(bucketInstance);

                    when(ecs.changeBucketPlan(BUCKET_NAME, serviceProxy, planProxy, parameters, bucketInstance.getServiceSettings()))
                        .thenReturn(new HashMap<>());
                });

                It("should change the plan", () -> {
                    workflow.changePlan(SERVICE_INSTANCE_ID, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                            .changeBucketPlan(SERVICE_INSTANCE_ID, serviceProxy, planProxy, parameters, bucketInstance.getServiceSettings());
                });
            });

            Context("#changePlan with custom name", () -> {
                BeforeEach(() -> {
                    when(instanceRepo.find(SERVICE_INSTANCE_ID))
                        .thenReturn(namedBucketInstance);

                    when(ecs.changeBucketPlan(BUCKET_NAME, serviceProxy, planProxy, parameters, namedBucketInstance.getServiceSettings()))
                        .thenReturn(new HashMap<>());
                });

                It("should change the plan", () -> {
                    workflow.changePlan(SERVICE_INSTANCE_ID, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                        .changeBucketPlan(BUCKET_NAME+"-"+SERVICE_INSTANCE_ID, serviceProxy, planProxy, parameters, namedBucketInstance.getServiceSettings());
                });
            });

            Context("#changePlan with bad instance", () -> {
                BeforeEach(() -> {
                    when(instanceRepo.find(SERVICE_INSTANCE_ID))
                        .thenReturn(null);
                });

                It("should throw Exception", () -> {
                    try {
                        workflow.changePlan(SERVICE_INSTANCE_ID, serviceProxy, planProxy, parameters);
                        fail("Expected InstanceDoesNotExistException");
                    } catch (ServiceInstanceDoesNotExistException e) {
                        assert e.getClass().equals(ServiceInstanceDoesNotExistException.class);
                    }
                });
            });

            Context("#delete with ReclaimPolicy", () -> {

                BeforeEach(() -> {
                    doNothing().when(instanceRepo).save(any(ServiceInstance.class));
                    when(instanceRepo.find(SERVICE_INSTANCE_ID)).thenReturn(bucketInstance);
                });

                Context("with no ReclaimPolicy", () -> {
                    It("should call delete and NOT wipe bucket", () -> {
                        CompletableFuture result = workflow.delete(SERVICE_INSTANCE_ID);
                        assertNull(result);
                        verify(ecs, times(1)).deleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                        verify(ecs, times(0)).wipeAndDeleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                    });
                });

                Context("with Fail ReclaimPolicy", () -> {
                    It("should call delete and NOT wipe bucket", () -> {
                        bucketInstance.setServiceSettings(Collections.singletonMap(RECLAIM_POLICY, ReclaimPolicy.Fail));

                        CompletableFuture result = workflow.delete(SERVICE_INSTANCE_ID);
                        assertNull(result);
                        verify(ecs, times(1)).deleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                        verify(ecs, times(0)).wipeAndDeleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                    });
                });

                Context("with Detach ReclaimPolicy", () -> {
                    It("should not call delete", () -> {
                        bucketInstance.setServiceSettings(Collections.singletonMap(RECLAIM_POLICY, ReclaimPolicy.Detach));

                        CompletableFuture result = workflow.delete(SERVICE_INSTANCE_ID);
                        assertNull(result);
                        verify(ecs, times(0)).deleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                        verify(ecs, times(0)).wipeAndDeleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                    });
                });

                Context("with Delete ReclaimPolicy", () -> {
                    It("should wipe and delete", () -> {
                        bucketInstance.setServiceSettings(Collections.singletonMap(RECLAIM_POLICY, ReclaimPolicy.Delete));

                        CompletableFuture result = workflow.delete(SERVICE_INSTANCE_ID);
                        assertNotNull(result);
                        verify(ecs, times(0)).deleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                        verify(ecs, times(1)).wipeAndDeleteBucket(bucketInstance.getName(), NAMESPACE_NAME);
                    });
                });
            });

            Context("#delete", () -> {

                BeforeEach(() -> doNothing().when(instanceRepo)
                        .save(any(ServiceInstance.class)));

                Context("with multiple references", () -> {
                    BeforeEach(() -> {
                        Set<String> refs = new HashSet<>(Arrays.asList(
                                SERVICE_INSTANCE_ID,
                                BUCKET_NAME + "2"
                        ));
                        bucketInstance.setReferences(refs);
                        when(instanceRepo.find(SERVICE_INSTANCE_ID))
                                .thenReturn(bucketInstance);
                        when(instanceRepo.find(BUCKET_NAME + "2"))
                                .thenReturn(bucketInstance);
                    });

                    Context("the bucket is included in references", () -> {
                        It("should not delete the bucket", () -> {
                            workflow.delete(SERVICE_INSTANCE_ID);
                            verify(ecs, times(0)).deleteBucket(SERVICE_INSTANCE_ID, NAMESPACE_NAME);
                        });

                        It("should update each references", () -> {
                            workflow.delete(SERVICE_INSTANCE_ID);
                            verify(instanceRepo, times(1))
                                    .save(instCaptor.capture());
                            ServiceInstance savedInst = instCaptor.getValue();
                            assertEquals(1, savedInst.getReferenceCount());
                            assert (savedInst.getReferences().contains(BUCKET_NAME + "2"));
                        });
                    });

                });

                Context("with a single reference", () -> {
                    BeforeEach(() -> {
                        Set<String> refs = new HashSet<>(Collections.singletonList(BUCKET_NAME));
                        bucketInstance.setReferences(refs);
                        when(instanceRepo.find(SERVICE_INSTANCE_ID))
                                .thenReturn(bucketInstance);
                    });

                    It("should delete the bucket", () -> {
                        workflow.delete(SERVICE_INSTANCE_ID);
                        verify(ecs, times(1))
                                .deleteBucket(SERVICE_INSTANCE_ID, NAMESPACE_NAME);
                    });
                });

                Context("with a custom name parameter", () -> {
                    BeforeEach(() -> {
                        when(instanceRepo.find(SERVICE_INSTANCE_ID))
                            .thenReturn(namedBucketInstance);
                    });

                    It("should delete the named bucket", () -> {
                        workflow.delete(SERVICE_INSTANCE_ID);
                        verify(ecs, times(1))
                            .deleteBucket(BUCKET_NAME+"-"+SERVICE_INSTANCE_ID, NAMESPACE_NAME);
                    });
                });
            });

            Context("#create", () -> {
                BeforeEach(() -> {
                    when(ecs.createBucket(BUCKET_NAME, BUCKET_NAME,  serviceProxy, planProxy, parameters))
                            .thenReturn(new HashMap<>());
                    workflow.withCreateRequest(bucketCreateRequestFixture(parameters));
                });

                It("should create the bucket", () -> {
                    workflow.create(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                            .createBucket(BUCKET_NAME, BUCKET_NAME, serviceProxy, planProxy, parameters);
                });

                It("should return the service instance", () -> {
                    ServiceInstance instance = workflow.create(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    assertEquals(BUCKET_NAME, instance.getName());
                    assertEquals(BUCKET_NAME, instance.getServiceInstanceId());
                });
            });

            Context("#create with name", () -> {
                BeforeEach(() -> {
                    when(ecs.createBucket(BUCKET_NAME, BUCKET_NAME,  serviceProxy, planProxy, parameters))
                        .thenReturn(new HashMap<>());

                    Map<String, Object> params = new HashMap<>();
                    params.put("name", BUCKET_NAME);
                    workflow.withCreateRequest(bucketCreateRequestFixture(params));
                });

                It("should create the bucket", () -> {
                    workflow.create(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                        .createBucket(BUCKET_NAME, BUCKET_NAME+"-"+BUCKET_NAME, serviceProxy, planProxy, parameters);
                });

                It("should return the service instance", () -> {
                    ServiceInstance instance = workflow.create(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    assertEquals(BUCKET_NAME+"-"+BUCKET_NAME, instance.getName());
                    assertEquals(BUCKET_NAME, instance.getServiceInstanceId());
                });
            });
        });
    }
}
