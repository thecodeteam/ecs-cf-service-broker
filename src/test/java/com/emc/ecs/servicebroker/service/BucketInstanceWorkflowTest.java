package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static com.emc.ecs.common.Fixtures.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.assertEquals;
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
    private final ArgumentCaptor<ServiceInstance> instCaptor = ArgumentCaptor.forClass(ServiceInstance.class);

    {
        Describe("BucketInstanceWorkflow", () -> {
            BeforeEach(() -> {
                ecs = mock(EcsService.class);
                instanceRepo = mock(ServiceInstanceRepository.class);
                workflow = new BucketInstanceWorkflow(instanceRepo, ecs);
            });

            Context("#changePlan", () -> {
                BeforeEach(() -> when(ecs.changeBucketPlan(BUCKET_NAME, serviceProxy, planProxy, parameters))
                        .thenReturn(new HashMap<>()));

                It("should change the plan", () -> {
                    workflow.changePlan(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                            .changeBucketPlan(BUCKET_NAME, serviceProxy, planProxy, parameters);
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
                            verify(ecs, times(0))
                                    .deleteBucket(SERVICE_INSTANCE_ID);
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
                        doNothing().when(ecs).deleteBucket(SERVICE_INSTANCE_ID);
                    });

                    It("should delete the bucket", () -> {
                        workflow.delete(SERVICE_INSTANCE_ID);
                        verify(ecs, times(1))
                                .deleteBucket(SERVICE_INSTANCE_ID);
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
        });
    }
}
