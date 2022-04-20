package com.emc.ecs.servicebroker.service;

import com.emc.ecs.common.Fixtures;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import com.emc.ecs.servicebroker.workflow.BindingWorkflow;
import com.emc.ecs.servicebroker.workflow.BucketBindingWorkflow;
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
import static com.emc.ecs.servicebroker.model.Constants.*;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.*;
import static org.junit.Assert.*;
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
                workflow = new BucketBindingWorkflow(instanceRepo, ecs)
                    .withCreateRequest(Fixtures.bucketBindingRequestFixture());
            });

            Context("with binding ID conflict", () -> {
                BeforeEach(() -> {
                            when(ecs.userExists(eq(BINDING_ID), eq(NAMESPACE_NAME))).thenReturn(true);
                            when(instanceRepo.find(eq(SERVICE_INSTANCE_ID))).thenReturn(serviceInstanceFixture());
                        }
                );

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
                    when(ecs.userExists(eq(BINDING_ID), eq(NAMESPACE_NAME))).thenReturn(false);
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
                            workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceFixture());
                            workflow.removeBinding();
                        } catch (ServiceInstanceDoesNotExistException e) {
                            assert e.getClass().equals(ServiceInstanceDoesNotExistException.class);
                        }
                    });

                    It("should throw a service-does-not-exist exception on getCredentials", () -> {
                        try {
                            workflow.getCredentials(SECRET_KEY_VALUE, new HashMap<>());
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

                        when(ecs.createUser(eq(BINDING_ID), anyString())).thenReturn(SECRET_KEY_VALUE);
                        when(ecs.createUser(eq(BUCKET_NAME+"-"+BINDING_ID), anyString())).thenReturn(SECRET_KEY_VALUE);

                        // Create credentials fixture
                        String s3Url = "http://" + URLEncoder.encode(BINDING_ID, "UTF-8")
                                + ":" + URLEncoder.encode(SECRET_KEY_VALUE.getSecretKey(), "UTF-8")
                                + "@127.0.0.1:9020/" + SERVICE_INSTANCE_ID;

                        credentials.put(CREDENTIALS_ACCESS_KEY, BINDING_ID);
                        credentials.put(CREDENTIALS_SECRET_KEY, SECRET_KEY_VALUE.getSecretKey());
                        credentials.put(ENDPOINT, OBJ_ENDPOINT);
                        credentials.put(BUCKET, SERVICE_INSTANCE_ID);
                        credentials.put(S3_URL, s3Url);
                        credentials.put(PATH_STYLE_ACCESS, true);
                    });

                    Context("basic bucket binding", () -> {

                        BeforeEach(() ->
                                doNothing().when(ecs).addUserToBucket(eq(SERVICE_INSTANCE_ID), anyString(), eq(BINDING_ID))
                        );

                        It("should create a new user", () -> {
                            workflow.createBindingUser();
                            verify(ecs, times(1))
                                    .createUser(BINDING_ID, NAMESPACE_NAME);
                        });

                        It("should add the user to a bucket", () -> {
                            workflow.createBindingUser();
                            verify(ecs, times(1))
                                    .addUserToBucket(eq(SERVICE_INSTANCE_ID), anyString(), eq(BINDING_ID));
                        });

                        It("should delete the user", () -> {
                            workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceFixture());
                            workflow.removeBinding();
                            verify(ecs, times(1))
                                    .deleteUser(BINDING_ID, NAMESPACE_NAME);
                        });

                        It("should remove the user from a bucket", () -> {
                            workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceFixture());
                            workflow.removeBinding();
                            verify(ecs, times(1))
                                    .removeUserFromBucket(SERVICE_INSTANCE_ID, NAMESPACE_NAME, BINDING_ID);
                        });

                        Context("create binding with custom name", () -> {
                            BeforeEach(() -> {
                                workflow = new BucketBindingWorkflow(instanceRepo, ecs)
                                    .withCreateRequest(Fixtures.bucketBindingRequestWithNameFixture(BUCKET_NAME));
                            });

                            It("should create a new named user", () -> {
                                workflow.createBindingUser();
                                verify(ecs, times(1))
                                    .createUser(BUCKET_NAME+"-"+BINDING_ID, NAMESPACE_NAME);
                            });

                            It("should add the named user to a bucket", () -> {
                                workflow.createBindingUser();
                                verify(ecs, times(1))
                                    .addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(BUCKET_NAME+"-"+BINDING_ID));
                            });

                            Context("with custom instance name", () -> {
                                BeforeEach(() -> {
                                    workflow = new BucketBindingWorkflow(instanceRepo, ecs)
                                        .withCreateRequest(Fixtures.bucketBindingRequestWithNameFixture(BUCKET_NAME));

                                    when(instanceRepo.find(eq(SERVICE_INSTANCE_ID))).thenReturn(serviceInstanceWithNameFixture(BUCKET_NAME));
                                });

                                It("should add the named user to a bucket", () -> {
                                    workflow.createBindingUser();
                                    verify(ecs, times(1))
                                        .addUserToBucket(eq(BUCKET_NAME+"-"+SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(BUCKET_NAME+"-"+BINDING_ID));
                                });
                            });
                        });

                        Context( "When path_style_access for broker set to false", () -> {
                            BeforeEach(() -> {
                                Map<String, Object> cfg = new HashMap<>();
                                cfg.put(PATH_STYLE_ACCESS, false);

                                when(ecs.getBrokerConfig()).thenReturn(cfg);
                            });

                            It("should create S3 domain-based URL when no user parameter is provided", () -> {
                                Map<String, Object> ret = workflow.getCredentials(new UserSecretKey("abcd"), new HashMap<>());
                                assertEquals(false, ret.get(PATH_STYLE_ACCESS));
                                assertEquals("Bucket binding workflow should generate domain-style url",
                                        "http://cf2f8326-3465-4810-9da1-54d328935b81:abcd@service-instance-id.127.0.0.1:9020",
                                        ret.get(S3_URL)
                                );
                            });

                            It("should create S3 path-based URL when user set path_style_access=true", () -> {
                                HashMap<String, Object> requestParam = new HashMap<>();
                                requestParam.put(PATH_STYLE_ACCESS, true);
                                Map<String, Object> ret = workflow.getCredentials(new UserSecretKey("abcd"), requestParam);
                                assertEquals(true, ret.get(PATH_STYLE_ACCESS));
                                assertEquals("Bucket binding workflow should generate path-style url",
                                        "http://cf2f8326-3465-4810-9da1-54d328935b81:abcd@127.0.0.1:9020/service-instance-id",
                                        ret.get(S3_URL)
                                );
                            });

                            It("should create S3 domain-based URL when user set path_style_access=false", () -> {
                                HashMap<String, Object> requestParam = new HashMap<>();
                                requestParam.put(PATH_STYLE_ACCESS, false);
                                Map<String, Object> ret = workflow.getCredentials(new UserSecretKey("abcd"), requestParam);
                                assertEquals(false, ret.get(PATH_STYLE_ACCESS));
                                assertEquals("Bucket binding workflow should generate domain-style url",
                                        "http://cf2f8326-3465-4810-9da1-54d328935b81:abcd@service-instance-id.127.0.0.1:9020",
                                        ret.get(S3_URL)
                                );
                            });
                        });

                        Context("delete binding with custom name", () -> {
                            It("should delete named user", () -> {
                                workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceWithNameFixture(BUCKET_NAME));
                                workflow.removeBinding();
                                verify(ecs, times(1))
                                    .deleteUser(BUCKET_NAME+"-"+BINDING_ID, NAMESPACE_NAME);
                            });

                            It("should remove the named user to a bucket", () -> {
                                workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceWithNameFixture(BUCKET_NAME));
                                workflow.removeBinding();
                                verify(ecs, times(1))
                                    .removeUserFromBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(BUCKET_NAME+"-"+BINDING_ID));
                            });

                            Context("with custom instance name", () -> {
                                BeforeEach(() -> {
                                    workflow = new BucketBindingWorkflow(instanceRepo, ecs)
                                        .withCreateRequest(Fixtures.bucketBindingRequestWithNameFixture(BUCKET_NAME));

                                    when(instanceRepo.find(eq(SERVICE_INSTANCE_ID))).thenReturn(serviceInstanceWithNameFixture(BUCKET_NAME));
                                });

                                It("should delete named user", () -> {
                                    workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceWithNameFixture(BUCKET_NAME));
                                    workflow.removeBinding();
                                    verify(ecs, times(1))
                                        .deleteUser(BUCKET_NAME+"-"+BINDING_ID, NAMESPACE_NAME);
                                });

                                It("should remove the named user to a bucket", () -> {
                                    workflow.withDeleteRequest(bucketBindingRemoveFixture(), bindingInstanceWithNameFixture(BUCKET_NAME));
                                    workflow.removeBinding();
                                    verify(ecs, times(1))
                                        .removeUserFromBucket(eq(BUCKET_NAME+"-"+SERVICE_INSTANCE_ID), anyString(), eq(BUCKET_NAME+"-"+BINDING_ID));
                                });
                            });
                        });

                        Context("with a port in the object endpoint", () ->
                                It("should return credentials", () -> {
                                    Map<String, Object> actual =
                                            workflow.getCredentials(SECRET_KEY_VALUE, parameters);
                                    assertCredentialsEqual(actual, credentials);
                                }));

                        Context("with no port in the object endpoint", () -> {
                            BeforeEach(() -> {
                                String host = "127.0.0.1";
                                String expectedEndpoint = "http://" + host;
                                when(ecs.getObjectEndpoint()).thenReturn(expectedEndpoint);
                                String expectedUrl =
                                        "http://" + URLEncoder.encode(BINDING_ID, "UTF-8")
                                        + ":" + URLEncoder.encode(SECRET_KEY_VALUE.getSecretKey(), "UTF-8")
                                        + "@" + host + "/" + SERVICE_INSTANCE_ID;
                                credentials.put(S3_URL, expectedUrl);
                                credentials.put(ENDPOINT, expectedEndpoint);
                            });

                            It("should return credentials", () -> {
                                Map<String, Object> actual = workflow.getCredentials(SECRET_KEY_VALUE, parameters);
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
                            parameters.put(USER_PERMISSIONS, Lists.newArrayList("READ", "READ_ACL"));

                            CreateServiceInstanceBindingRequest req = bucketBindingRequestFixture(parameters);
                            workflow = workflow.withCreateRequest(req);

                            doNothing().when(ecs).addUserToBucket(
                                    eq(SERVICE_INSTANCE_ID),eq(BINDING_ID), eq(NAMESPACE_NAME), any(listClass)
                            );
                        });

                        It("should add the user to the bucket with ACL", () -> {
                            workflow.createBindingUser();
                            verify(ecs, times(1))
                                    .addUserToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), eq(BINDING_ID), permsCaptor.capture());
                            List perms = permsCaptor.getValue();
                            assertEquals(2, perms.size());
                            assertEquals("READ", perms.get(0));
                            assertEquals("READ_ACL", perms.get(1));
                        });
                    });

                    Context("without path style access in binding", () -> {
                        BeforeEach(() -> {
                            parameters.put(PATH_STYLE_ACCESS, false);

                            CreateServiceInstanceBindingRequest req = bucketBindingRequestFixture(parameters);
                            workflow = workflow.withCreateRequest(req);

                            String s3Url = "http://" + URLEncoder.encode(BINDING_ID, "UTF-8")
                                    + ":" + URLEncoder.encode(SECRET_KEY_VALUE.getSecretKey(), "UTF-8")
                                    + "@" + SERVICE_INSTANCE_ID + ".127.0.0.1:9020";
                            credentials.put(S3_URL, s3Url);
                            credentials.put(PATH_STYLE_ACCESS, false);
                        });

                        It("should return credentials with host-style access", () -> {
                            Map<String, Object> actual = workflow.getCredentials(SECRET_KEY_VALUE, parameters);
                            assertCredentialsEqual(actual, credentials);
                        });
                    });

                    Context("with volume mount", () -> {
                        BeforeEach(() ->
                                when(ecs.getBucketFileEnabled(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME)))
                                        .thenReturn(true));

                        Context("at bucket root", () -> {
                            BeforeEach(() -> {
                                String path = "/ns1/" + SERVICE_INSTANCE_ID + "/";
                                when(ecs.addExportToBucket(eq(SERVICE_INSTANCE_ID), anyString(), anyString()))
                                        .thenReturn(path);
                                doNothing().when(ecs).deleteUserMap(eq(BINDING_ID), anyString(), anyString());
                            });

                            It("should create an NFS export", () -> {
                                workflow.createBindingUser();

                                verify(ecs, times(1))
                                        .getBucketFileEnabled(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME));
                                verify(ecs, times(1))
                                        .createUser(BINDING_ID, NAMESPACE_NAME);
                                verify(ecs, times(1))
                                        .addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), pathCaptor.capture());
                                assertNull(pathCaptor.getValue());
                            });

                            It("should delete the NFS export", () -> {
                                ServiceInstanceBinding existingBinding = bindingInstanceVolumeMountFixture();
                                workflow.withDeleteRequest(bucketBindingRemoveFixture(), existingBinding);
                                workflow.removeBinding();

                                verify(ecs, times(1))
                                        .deleteUser(existingBinding.getName(), NAMESPACE_NAME);
                                verify(ecs, times(1))
                                        .deleteUserMap(eq(existingBinding.getName()), eq(NAMESPACE_NAME), eq("456"));
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

                            It("should provide UID in credentials", () -> {
                                UserSecretKey secretKey = workflow.createBindingUser();
                                Map<String, Object> binding_credentials =
                                        workflow.getCredentials(secretKey, parameters);
                                assertTrue(binding_credentials.containsKey(VOLUME_EXPORT_UID));
                            });
                        });

                        Context("at nested path", () -> {
                            BeforeEach(() -> {
                                parameters.put(VOLUME_EXPORT, "some-path");
                                workflow = workflow.withCreateRequest(bucketBindingRequestFixture(parameters));
                                String path = "/ns1/" + SERVICE_INSTANCE_ID + "/some-path";
                                when(ecs.addExportToBucket(eq(SERVICE_INSTANCE_ID), anyString(), anyString()))
                                        .thenReturn(path);
                            });

                            It("should create an NFS export", () -> {
                                workflow.createBindingUser();

                                verify(ecs, times(1))
                                        .createUser(BINDING_ID, NAMESPACE_NAME);
                                verify(ecs, times(1))
                                        .addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), pathCaptor.capture());
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
                                            .addExportToBucket(eq(SERVICE_INSTANCE_ID), eq(NAMESPACE_NAME), pathCaptor.capture());
                                    assertEquals(pathCaptor.getValue(), null);
                                });

                            });
                        });
                    });
                });
            });
        });
    }

    private static void assertCredentialsEqual(Map<String, Object> actual, Map<String, Object> expected) {
        assertEquals(expected.get(CREDENTIALS_ACCESS_KEY), actual.get(CREDENTIALS_ACCESS_KEY));
        assertEquals(expected.get(CREDENTIALS_SECRET_KEY), actual.get(CREDENTIALS_SECRET_KEY));
        assertEquals(expected.get(ENDPOINT), actual.get(ENDPOINT));
        assertEquals(expected.get(BUCKET), actual.get(BUCKET));
        assertEquals(expected.get(S3_URL), actual.get(S3_URL));
        assertEquals(expected.get(PATH_STYLE_ACCESS), actual.get(PATH_STYLE_ACCESS));
    }
}
