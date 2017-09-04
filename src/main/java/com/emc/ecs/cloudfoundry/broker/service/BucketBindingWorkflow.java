package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.VolumeMount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class BucketBindingWorkflow extends BindingWorkflowImpl {
    private static final String VOLUME_DRIVER = "nfsv3driver";
    private static final String DEFAULT_CONTAINER_DIR = "/var/vcap/data";
    private List<VolumeMount> volumeMounts;
    private URL baseUrl;
    private static final Logger LOG =
            LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    BucketBindingWorkflow(EcsService ecs) throws MalformedURLException {
        super(ecs);
        String endpoint = ecs.getObjectEndpoint();
        baseUrl = new URL(endpoint);
    }

    @Override
    public UserSecretKey createBindingUser() throws EcsManagementClientException {
        UserSecretKey userSecretKey = ecs.createUser(bindingId);
        Map<String, Object> parameters = createRequest.getParameters();

        if (parameters != null) {
            @SuppressWarnings(value = "unchecked")
            List<String> permissions = (List<String>) parameters.get("permissions");
            if (permissions == null) {
                ecs.addUserToBucket(instanceId, bindingId);
            } else {
                ecs.addUserToBucket(instanceId, bindingId, permissions);
            }

            String export = (String) parameters.get("export");
            if (ecs.getBucketFileEnabled(instanceId) && export != null)
                volumeMounts = createVolumeExport(export, baseUrl);

        } else {
            ecs.addUserToBucket(instanceId, bindingId);
        }

        return userSecretKey;
    }

    @Override
    public void removeBinding(ServiceInstanceBinding binding)
            throws EcsManagementClientException {
        List<VolumeMount> volumes = binding.getVolumeMounts();
        if (volumes != null && volumes.size() > 0) {
            Map<String, Object> mountConfig = (
                        (SharedVolumeDevice) volumes.get(0).getDevice()
                    ).getMountConfig();
            String unixId = (String) mountConfig.get("uid");
            LOG.error("Deleting user map of instance Id and Binding Id " +
                    instanceId + " " + bindingId);
            ecs.deleteUserMap(bindingId, unixId);
        }

        ecs.removeUserFromBucket(instanceId, bindingId);
        ecs.deleteUser(bindingId);
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey)
            throws MalformedURLException, EcsManagementClientException {
        Map<String, Object> credentials = new HashMap<>();
        String endpoint = ecs.getObjectEndpoint();

        credentials.put("bucket", ecs.prefix(instanceId));
        credentials.put("s3Url", getS3Url(baseUrl, secretKey));
        credentials.put("accessKey", ecs.prefix(bindingId));
        credentials.put("endpoint", endpoint);
        credentials.put("secretKey", secretKey);

        return credentials;
    }

    @Override
    public ServiceInstanceBinding getBinding(Map<String, Object> credentials) {
        ServiceInstanceBinding binding = new ServiceInstanceBinding(createRequest);
        binding.setBindingId(bindingId);
        binding.setCredentials(credentials);
        if (volumeMounts != null)
            binding.setVolumeMounts(volumeMounts);
        return binding;
    }

    @Override
    public CreateServiceInstanceAppBindingResponse getResponse(
            Map<String, Object> credentials) {
        CreateServiceInstanceAppBindingResponse resp =
                new CreateServiceInstanceAppBindingResponse()
                .withCredentials(credentials);
        if (volumeMounts != null)
            resp = resp.withVolumeMounts(volumeMounts);

        return resp;
    }

    private String getS3Url(URL baseUrl, String secretKey) {
        String userInfo = getUserInfo(secretKey);
        return baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@" +
                baseUrl.getHost() + ":" + baseUrl.getPort() + "/" +
                ecs.prefix(instanceId);
    }

    private int createUserMap() throws EcsManagementClientException {
        int unixUid = (int) (2000 + System.currentTimeMillis() % 8000);
        while (true) {
            try {
                ecs.createUserMap(bindingId, unixUid);
                break;
            } catch (EcsManagementClientException e) {
                if (e.getMessage().contains("Bad request body (1013)")) {
                    unixUid++;
                } else {
                    throw e;
                }
            }
        }
        return unixUid;
    }

    private List<VolumeMount> createVolumeExport(String export, URL baseUrl)
            throws EcsManagementClientException {
        int unixUid = createUserMap();
        String host = ecs.getNfsMountHost();
        if (host == null || host.isEmpty()) {
            host = baseUrl.getHost();
        }
        String volumeGUID = UUID.randomUUID().toString();
        String absoluteExportPath = ecs.addExportToBucket(instanceId, export);
        Map<String, Object> opts = new HashMap<>();
        String nfsUrl = "nfs://" + host + absoluteExportPath;
        opts.put("source", nfsUrl);
        opts.put("uid", String.valueOf(unixUid));
        List<VolumeMount> mounts = new ArrayList<>();
        mounts.add(new VolumeMount(VOLUME_DRIVER, DEFAULT_CONTAINER_DIR,
                VolumeMount.Mode.READ_WRITE, VolumeMount.DeviceType.SHARED,
                new SharedVolumeDevice(volumeGUID, opts)));
        return mounts;
    }
}
