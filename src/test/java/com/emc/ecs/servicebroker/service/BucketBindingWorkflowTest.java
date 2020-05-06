package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;
import org.assertj.core.util.Lists;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.common.Fixtures.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(Ginkgo4jRunner.class)
public class BucketBindingWorkflowTest {
    private EcsService ecs;
    private ServiceInstanceRepository instanceRepo;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, Object> credentials = new HashMap<>();
    @SuppressWarnings("unchecked")
    private Class<List<String>> listClass = (Class<List<String>>) (Class) ArrayList.class;
    private ArgumentCaptor<List<String>> permsCaptor = ArgumentCaptor.forClass(listClass);
    private ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
    private BindingWorkflow workflow;

    {
        Describe("BucketBindingWorkflow", () -> {
            BeforeEach(() -> {
                ecs = mock(EcsService.class);
                instanceRepo = mock(ServiceInstanceRepository.class);
                workflow = new BucketBindingWorkflow(instanceRepo, ecs);
            });

            Context("with binding ID conflict", () -> {
                BeforeEach(() ->
                        when(ecs.userExists(eq(BINDING_ID))).thenReturn(true));

                It("should throw an binding-exists exception", () -> {
                    try {
                        workflow.checkIfUserExists();
                    } catch (ServiceInstanceBindingExistsException e) {
                        assert e.getClass().equals(ServiceInstanceBindingExistsException.class);
                    }
                });
            });

            Context("without binding ID conflict", () -> {
                BeforeEach(() -> {
                    CreateServiceInstanceBindingRequest req = bucketBindingRequestFixture();
                    workflow = workflow.withCreateRequest(req);
                    when(ecs.userExists(eq(BINDING_ID))).thenReturn(false);
                });

                Context("when the service instance doesn't exist", () -> {
                    BeforeEach(() ->
                            when(instanceRepo.find(eq(SERVICE_INSTANCE_ID))).thenReturn(null));

                    It("should throw a service-does-not-exist exception on createBindingUser", () -> {
                        try {
                            workflow.createBindingUser();
                        } catch (ServiceInstanceDoesNotExistException e) {
                            assert e.getClass().equals(ServiceInstanceDoesNotExistException.class);
                        }
                    });

                    It("should throw a service-does-not-exist exception on removeBinding", () -> {
                        try {
                            workflow.removeBinding(bindingInstanceFixture());
                        } catch (ServiceInstanceDoesNotExistException e) {
                            assert e.getClass().equals(ServiceInstanceDoesNotExistException.class);
                        }
                    });

                    It("should throw a service-does-not-exist exception on getCredentials", () -> {
                        try {
                            workflow.getCredentials(SECRET_KEY, new HashMap<>());
                        } catch (ServiceInstanceDoesNotExistException e) {
                            assert e.getClass().equals(ServiceInstanceDoesNotExistException.class);
                        }
                    });

                });


                Context("when the service instance exists", () -> {
                    BeforeEach(() -> {
                        // Mock out all prefix calls to return prefixed argument
                        when(ecs.prefix(anyString())).thenAnswer((Answer<String>) invocation -> {
                            Object[] args = invocation.getArguments();
                            return (String) args[0];
                        });

                        // Return a default endpoint
                        when(ecs.getObjectEndpoint()).thenReturn(OBJ_ENDPOINT);

                        // Mock service instance repo lookups
                        when(instanceRepo.find(eq(SERVICE_INSTANCE_ID))).thenReturn(serviceInstanceFixture());

                        UserSecretKey userSecretKey = new UserSecretKey();
                        userSecretKey.setSecretKey(SECRET_KEY);
                        when(ecs.createUser(eq(BINDING_ID))).thenReturn(userSecretKey);

                        // Create credentials fixture
                        String s3Url = "http://" + URLEncoder.encode(BINDING_ID, "UTF-8")
                                + ":" + URLEncoder.encode(SECRET_KEY, "UTF-8")
                                + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID;
                        credentials.put("accessKey", BINDING_ID);
                        credentials.put("secretKey", SECRET_KEY);
                        credentials.put("endpoint", OBJ_ENDPOINT);
                        credentials.put("bucket", SERVICE_INSTANCE_ID);
                        credentials.put("s3Url", s3Url);
                        credentials.put("path-style-access", true);
                    });

                    Context("basic bucket binding", () -> {

                        BeforeEach(() ->
                                doNothing().when(ecs).addUserToBucket(eq(SERVICE_INSTANCE_ID),
                                        eq(BINDING_ID)));

                        It("should create a new user", () -> {
                            workflow.createBindingUser();
                            verify(ecs, times(1))
                                    .createUser(BINDING_ID);
                        });

                        It("should add the user to a bucket", () -> {
                            workflow.createBindingUser();
                            verify(ecs, times(1))
                                    .addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID));
                        });

                        It("should delete the user", () -> {
                            workflow.removeBinding(bindingInstanceFixture());
                            verify(ecs, times(1))
                                    .deleteUser(BINDING_ID);
                        });

                        It("should remove the user from a bucket", () -> {
                            workflow.removeBinding(bindingInstanceFixture());
                            verify(ecs, times(1))
                                    .removeUserFromBucket(SERVICE_INSTANCE_ID, BINDING_ID);
                        });


                        Context("with a port in the object endpoint", () ->
                                It("should return credentials", () -> {
                                    Map<String, Object> actual =
                                            workflow.getCredentials(SECRET_KEY, parameters);
                                    assertCredentialsEqual(actual, credentials);
                                }));

                        Context("with no port in the object endpoint", () -> {
                            BeforeEach(() -> {
                                String host = "127.0.0.1";
                                String expectedEndpoint = "http://" + host;
                                when(ecs.getObjectEndpoint()).thenReturn(expectedEndpoint);
                                String expectedUrl =
                                        "http://" + URLEncoder.encode(BINDING_ID, "UTF-8")
                                        + ":" + URLEncoder.encode(SECRET_KEY, "UTF-8")
                                        + "@" + host + "/" + SERVICE_INSTANCE_ID;
                                credentials.put("s3Url", expectedUrl);
                                credentials.put("endpoint", expectedEndpoint);
                            });

                            It("should return credentials", () -> {
                                Map<String, Object> actual = workflow.getCredentials(SECRET_KEY, parameters);
                                assertCredentialsEqual(actual, credentials);
                            });

                        });

                        It("should return a binding", () -> {
                            ServiceInstanceBinding binding = workflow.getBinding(credentials);
                            assertEquals(binding.getBindingId(), BINDING_ID);
                            assertCredentialsEqual(binding.getCredentials(), credentials);
                            assertEquals(binding.getServiceDefinitionId(), BUCKET_SERVICE_ID);
                            assertEquals(binding.getPlanId(), BUCKET_PLAN_ID1);
                        });

                        It("should return a response", () -> {
                            CreateServiceInstanceAppBindingResponse resp = workflow.getResponse(credentials);
                            assertCredentialsEqual(resp.getCredentials(), credentials);
                        });
                    });

                    Context("permissions bucket binding", () -> {
                        BeforeEach(() -> {
                            // Add params to workflow
                            List<String> readOnlyPerms =
                                    Lists.newArrayList("READ", "READ_ACL");
                            parameters.put("permissions", readOnlyPerms);

                            CreateServiceInstanceBindingRequest req = bucketBindingRequestFixture(parameters);
                            workflow = workflow.withCreateRequest(req);

                            doNothing().when(ecs).addUserToBucket(eq(SERVICE_INSTANCE_ID),
                                    eq(BINDING_ID), any(listClass));
                        });

                        It("should add the user to the bucket with ACL", () -> {
                            workflow.createBindingUser();
                            verify(ecs, times(1))
                                    .addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(BINDING_ID),
                                            permsCaptor.capture());
                            List perms = permsCaptor.getValue();
                            assertEquals(2, perms.size());
                            assertEquals("READ", perms.get(0));
                            assertEquals("READ_ACL", perms.get(1));
                        });
                    });

                    Context("without path style access in binding", () -> {
                        BeforeEach(() -> {
                            parameters.put("path-style-access", false);

                            CreateServiceInstanceBindingRequest req = bucketBindingRequestFixture(parameters);
                            workflow = workflow.withCreateRequest(req);

                            String s3Url = "http://" + URLEncoder.encode(BINDING_ID, "UTF-8")
                                    + ":" + URLEncoder.encode(SECRET_KEY, "UTF-8")
                                    + "@" + SERVICE_INSTANCE_ID + ".127.0.0.1:9020";
                            credentials.put("s3Url", s3Url);
                            credentials.put("path-style-access", false);
                        });

                        It("should return credentials with host-style access", () -> {
                            Map<String, Object> actual = workflow.getCredentials(SECRET_KEY, parameters);
                            assertCredentialsEqual(actual, credentials);
                        });
                    });

                    Context("with volume mount", () -> {
                        BeforeEach(() ->
                                when(ecs.getBucketFileEnabled(eq(SERVICE_INSTANCE_ID)))
                                        .thenReturn(true));

                        Context("at bucket root", () -> {
                            BeforeEach(() -> {
                                String path = "/ns1/" + SERVICE_INSTANCE_ID + "/";
                                when(ecs.addExportToBucket(eq(SERVICE_INSTANCE_ID), any(String.class)))
                                        .thenReturn(path);
                                doNothing().when(ecs).deleteUserMap(eq(BINDING_ID), any(String.class));
                            });

                            It("should create an NFS export", () -> {
                                workflow.createBindingUser();

                                verify(ecs, times(1))
                                        .getBucketFileEnabled(eq(SERVICE_INSTANCE_ID));
                                verify(ecs, times(1))
                                        .createUser(eq(BINDING_ID));
                                verify(ecs, times(1))
                                        .addExportToBucket(eq(SERVICE_INSTANCE_ID), pathCaptor.capture());
                                assertNull(pathCaptor.getValue());
                            });

                            It("should delete the NFS export", () -> {
                                workflow.removeBinding(bindingInstanceVolumeMountFixture());
                                verify(ecs, times(1))
                                        .deleteUser(BINDING_ID);
                                verify(ecs, times(1))
                                        .deleteUserMap(eq(BINDING_ID), eq("456"));
                            });

                            It("should return a binding mount", () -> {
                                workflow.createBindingUser();
                                ServiceInstanceBinding binding = workflow.getBinding(credentials);

                                assertEquals(binding.getBindingId(), BINDING_ID);

                                List<VolumeMount> mounts = binding.getVolumeMounts();
                                assertEquals(1, mounts.size());

                                String containerDir = "/var/vcap/data" + File.separator + BINDING_ID;
                                assertEquals(containerDir, mounts.get(0).getContainerDir());
                            });

                            It("should return a response with mount", () -> {
                                workflow.createBindingUser();
                                CreateServiceInstanceAppBindingResponse resp =
                                        workflow.getResponse(credentials);

                                List<VolumeMount> mounts = resp.getVolumeMounts();
                                assertEquals(1, mounts.size());

                                String containerDir = "/var/vcap/data" + File.separator + BINDING_ID;
                                assertEquals(containerDir, mounts.get(0).getContainerDir());
                            });
                        });

                        Context("at nested path", () -> {
                            BeforeEach(() -> {
                                parameters.put("export", "some-path");
                                workflow = workflow.withCreateRequest(bucketBindingRequestFixture(parameters));
                                String path = "/ns1/" + SERVICE_INSTANCE_ID + "/some-path";
                                when(ecs.addExportToBucket(eq(SERVICE_INSTANCE_ID), any(String.class)))
                                        .thenReturn(path);
                            });

                            It("should create an NFS export", () -> {
                                workflow.createBindingUser();

                                verify(ecs, times(1))
                                        .createUser(eq(BINDING_ID));
                                verify(ecs, times(1))
                                        .addExportToBucket(eq(SERVICE_INSTANCE_ID), pathCaptor.capture());
                                assertEquals(pathCaptor.getValue(), "some-path");
                            });


                            Context("with a null export parameter", () -> {
                                BeforeEach(() -> {
                                    parameters.put("export", null);
                                    workflow = workflow.withCreateRequest(bucketBindingRequestFixture(parameters));
                                });

                                It("should create an NFS export with no path", () -> {
                                    workflow.createBindingUser();

                                    verify(ecs, times(1))
                                            .addExportToBucket(eq(SERVICE_INSTANCE_ID), pathCaptor.capture());
                                    assertEquals(pathCaptor.getValue(), null);
                                });

                            });
                        });
                    });
                });
            });
        });
    }

    private static void assertCredentialsEqual(Map<String, Object> actual, Map<String, Object> credentials) {
        assertEquals(credentials.get("accessKey"), actual.get("accessKey"));
        assertEquals(credentials.get("secretKey"), actual.get("secretKey"));
        assertEquals(credentials.get("endpoint"), actual.get("endpoint"));
        assertEquals(credentials.get("bucket"), actual.get("bucket"));
        assertEquals(credentials.get("s3Url"), actual.get("s3Url"));
        assertEquals(credentials.get("path-style-access"), actual.get("path-style-access"));
    }
}
