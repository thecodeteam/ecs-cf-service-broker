package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;

import java.util.*;

import static com.emc.ecs.common.Fixtures.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(Ginkgo4jRunner.class)
public class RemoteConnectionInstanceWorkflowTest {

    private EcsService ecs;
    private ServiceInstanceRepository instanceRepo;
    private Map<String, Object> parameters = new HashMap<>();
    private InstanceWorkflow workflow;
    private ServiceDefinitionProxy serviceProxy = new ServiceDefinitionProxy();
    private PlanProxy planProxy = new PlanProxy();
    private ServiceInstance bucketInstance = serviceInstanceFixture();
    private ArgumentCaptor<ServiceInstance> instCaptor = ArgumentCaptor.forClass(ServiceInstance.class);

    {
        Describe("RemoteConnectionInstanceWorkflow", () -> {
            BeforeEach(() -> {
                ecs = mock(EcsService.class);
                instanceRepo = mock(ServiceInstanceRepository.class);
                workflow = new RemoteConnectionInstanceWorkflow(instanceRepo, ecs);
            });

            Context("#changePlan", () -> {

                It("should throw an exception as this operation isn't supported", () -> {
                    try {
                        workflow.changePlan(BUCKET_NAME, serviceProxy, planProxy, parameters);
                    } catch (ServiceBrokerException e) {
                        assertEquals(e.getClass(), ServiceBrokerException.class);
                    }
                });

            });

            Context("#delete", () -> {

                It("should throw an exception as this operation isn't supported", () -> {
                    try {
                        workflow.delete(BUCKET_NAME);
                    } catch (ServiceBrokerException e) {
                        assertEquals(e.getClass(), ServiceBrokerException.class);
                    }
                });

            });

            Context("#create", () -> {

                BeforeEach(() -> {
                    HashMap<Object, Object> remoteConnection = new HashMap<>();
                    remoteConnection.put("instanceId", BUCKET_NAME + "2");
                    parameters.put("remote_connection", remoteConnection);
                    workflow.withCreateRequest(bucketCreateRequestFixture(parameters));
                });

                Context("when remote instance doesn't exist", () -> {

                    BeforeEach(() -> {
                        when(instanceRepo.find(BUCKET_NAME))
                                .thenReturn(null);
                    });

                    It("should raise an exception", () -> {
                        try {
                            workflow.create(BUCKET_NAME, serviceProxy, planProxy, parameters);
                        } catch (ServiceBrokerException e) {
                            String message = "remote instance doesn't exist in repository";
                            assertEquals(ServiceBrokerException.class, e.getClass());
                            assertEquals(message, e.getMessage());
                        }
                    });


                });

                Context("when service definitions don't match", () -> {
                    BeforeEach(() -> {
                        when(instanceRepo.find(BUCKET_NAME + "2"))
                                .thenReturn(new ServiceInstance(bucketCreateRequestFixture(parameters)));
                        ServiceDefinitionProxy serviceDef = bucketServiceFixture();
                        Map<String, Object> settings = new HashMap<>();
                        settings.put("encrypted", true);
                        serviceDef.setServiceSettings(settings);
                        when(ecs.lookupServiceDefinition(BUCKET_SERVICE_ID))
                                .thenReturn(serviceDef);
                    });

                });
            });

        });

   }
}
