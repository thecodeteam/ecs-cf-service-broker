package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceRepository;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.VolumeMount;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BucketBindingWorkflow extends BindingWorkflowImpl {
    private static final String VOLUME_DRIVER = "nfsv3driver";
    private static final String DEFAULT_CONTAINER_DIR = "/var/vcap/data";
    private List<VolumeMount> volumeMounts;
    private static final Logger LOG =
            LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    BucketBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) throws IOException {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        if (ecs.userExists(bindingId))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        UserSecretKey userSecretKey = ecs.createUser(bindingId);
        Map<String, Object> parameters = createRequest.getParameters();
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        String bucketName = instance.getName();

        if (parameters != null) {

            @SuppressWarnings(value = "unchecked")
            List<String> permissions = (List<String>) parameters.get("permissions");
            if (permissions == null) {
                ecs.addUserToBucket(bucketName, bindingId);
            } else {
                ecs.addUserToBucket(bucketName, bindingId, permissions);
            }

            String export = (String) parameters.get("export");
            if (ecs.getBucketFileEnabled(bucketName) && export != null) {
                volumeMounts = createVolumeExport(export, new URL(ecs.getObjectEndpoint()));
            }

        } else {
            ecs.addUserToBucket(bucketName, bindingId);
        }

        return userSecretKey.getSecretKey();
    }

    @Override
    public void removeBinding(ServiceInstanceBinding binding)
            throws EcsManagementClientException, IOException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        String bucketName = instance.getName();
        List<VolumeMount> volumes = binding.getVolumeMounts();
        if (volumes != null && volumes.size() > 0) {
            Map<String, Object> mountConfig = (
                        (SharedVolumeDevice) volumes.get(0).getDevice()
                    ).getMountConfig();
            String unixId = (String) mountConfig.get("uid");
            LOG.error("Deleting user map of instance Id and Binding Id " +
                    bucketName + " " + bindingId);
            ecs.deleteUserMap(bindingId, unixId);
        }

        ecs.removeUserFromBucket(bucketName, bindingId);
        ecs.deleteUser(bindingId);
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey)
            throws IOException, EcsManagementClientException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);
        String bucketName = instance.getName();

        Map<String, Object> credentials = super.getCredentials(secretKey);

        // Add default broker endpoint
        String endpoint = ecs.getObjectEndpoint();
        credentials.put("endpoint", endpoint);

        // Add s3 URL
        URL baseUrl = new URL(endpoint);
        credentials.put("s3Url", getS3Url(baseUrl, secretKey));

        // Add bucket name from repository
        credentials.put("bucket", ecs.prefix(bucketName));

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
