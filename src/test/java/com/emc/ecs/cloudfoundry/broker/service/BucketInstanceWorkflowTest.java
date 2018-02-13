package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
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
    private Map<String, Object> parameters = new HashMap<>();
    private InstanceWorkflow workflow;
    private ServiceDefinitionProxy serviceProxy = new ServiceDefinitionProxy();
    private PlanProxy planProxy = new PlanProxy();
    private ServiceInstance bucketInstance = serviceInstanceFixture();
    private ArgumentCaptor<ServiceInstance> instCaptor = ArgumentCaptor.forClass(ServiceInstance.class);

    {
        Describe("BucketInstanceWorkflow", () -> {
            BeforeEach(() -> {
                ecs = mock(EcsService.class);
                instanceRepo = mock(ServiceInstanceRepository.class);
                workflow = new BucketInstanceWorkflow(instanceRepo, ecs);
            });

            Context("#changePlan", () -> {
                BeforeEach(() -> {
                    when(ecs.changeBucketPlan(BUCKET_NAME, serviceProxy, planProxy, parameters))
                            .thenReturn(new HashMap<>());
                });

                It("should change the plan", () -> {
                    workflow.changePlan(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                            .changeBucketPlan(BUCKET_NAME, serviceProxy, planProxy, parameters);
                });
            });

            Context("#delete", () -> {

                BeforeEach(() -> {
                    doNothing().when(instanceRepo)
                            .save(any(ServiceInstance.class));
                });

                Context("with multiple references", () -> {
                    BeforeEach(() -> {
                        Set<String> refs = new HashSet<>(Arrays.asList(
                                BUCKET_NAME,
                                BUCKET_NAME + "2"
                        ));
                        bucketInstance.setReferences(refs);
                        when(instanceRepo.find(BUCKET_NAME))
                                .thenReturn(bucketInstance);
                        when(instanceRepo.find(BUCKET_NAME + "2"))
                                .thenReturn(bucketInstance);
                    });

                    It("should not delete the bucket" ,() -> {
                        workflow.delete(BUCKET_NAME);
                        verify(ecs, times(0))
                                .deleteBucket(BUCKET_NAME);
                    });

                    It("should update each references", () -> {
                        workflow.delete(BUCKET_NAME);
                        verify(instanceRepo, times(1))
                                .save(instCaptor.capture());
                        ServiceInstance savedInst = instCaptor.getValue();
                        assertEquals(1, savedInst.getReferenceCount());
                        assert(savedInst.getReferences().contains(BUCKET_NAME + "2"));
                    });

                });

                Context("with a single reference", () -> {
                    BeforeEach(() -> {
                        Set<String> refs = new HashSet<>(Collections.singletonList(BUCKET_NAME));
                        bucketInstance.setReferences(refs);
                        when(instanceRepo.find(BUCKET_NAME))
                                .thenReturn(bucketInstance);
                        doNothing().when(ecs).deleteBucket(BUCKET_NAME);
                    });

                    It("should delete the bucket", () -> {
                        workflow.delete(BUCKET_NAME);
                        verify(ecs, times(1))
                                .deleteBucket(BUCKET_NAME);
                    });
                });
            });

            Context("#create", () -> {
                BeforeEach(() -> {
                    when(ecs.createBucket(BUCKET_NAME, serviceProxy, planProxy, parameters))
                            .thenReturn(new HashMap<>());
                    workflow.withCreateRequest(bucketCreateRequestFixture(parameters));
                });

                It("should create the bucket", () -> {
                    workflow.create(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    verify(ecs, times(1))
                            .createBucket(BUCKET_NAME, serviceProxy, planProxy, parameters);
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
