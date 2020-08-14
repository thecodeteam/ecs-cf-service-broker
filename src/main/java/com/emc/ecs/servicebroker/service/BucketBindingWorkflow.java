package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class BucketBindingWorkflow extends BindingWorkflowImpl {
    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    private static final String VOLUME_DRIVER = "nfsv3driver";
    private static final String DEFAULT_MOUNT = "/var/vcap/data";
    private static final String MOUNT = "mount";

    private List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();

    BucketBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) throws IOException {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        if (ecs.userExists(binding.getName()))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        String bucketName = getInstanceName();

        UserSecretKey userSecretKey = ecs.createUser(binding.getName());

        Map<String, Object> parameters = createRequest.getParameters();

        String export = "";
        List<String> permissions = null;
        if (parameters != null) {
            permissions = (List<String>) parameters.get("permissions");
            export = (String) parameters.getOrDefault("export", null);
        }

        if (permissions == null) {
            ecs.addUserToBucket(bucketName, binding.getName());
        } else {
            ecs.addUserToBucket(bucketName, binding.getName(), permissions);
        }

        if (ecs.getBucketFileEnabled(bucketName)) {
            volumeMounts = createVolumeExport(export, new URL(ecs.getObjectEndpoint()), bucketName, parameters);
        }

        return userSecretKey.getSecretKey();
    }

    @Override
    public void removeBinding() throws EcsManagementClientException, IOException {
        String bucketName = getInstanceName();

        List<VolumeMount> volumes = binding.getVolumeMounts();

        if (volumes != null && volumes.size() > 0) {
            Map<String, Object> mountConfig = ((SharedVolumeDevice) volumes.get(0).getDevice()).getMountConfig();
            String unixId = (String) mountConfig.get("uid");

            LOG.info("Deleting user map for bucket '{}' binding '{}' ", bucketName, bindingId);

            try {
                ecs.deleteUserMap(binding.getName(), unixId);
            } catch (EcsManagementClientException e) {
                LOG.error("Error deleting user map: " + e.getMessage());
            }
        }

        LOG.info("Removing binding user {} from bucket {}", bindingId, bucketName);

        ecs.removeUserFromBucket(bucketName, binding.getName());
        ecs.deleteUser(binding.getName());
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey, Map<String, Object> parameters) throws IOException, EcsManagementClientException {
        String bucketName = getInstanceName();

        Map<String, Object> credentials = super.getCredentials(secretKey);

        // Add default broker endpoint
        String endpoint = ecs.getObjectEndpoint();
        credentials.put("endpoint", endpoint);

        // Add s3 URL
        URL baseUrl = new URL(endpoint);
        credentials.put("s3Url", getS3Url(baseUrl, secretKey, parameters));

        if (parameters != null && parameters.containsKey("path-style-access") && !(Boolean) parameters.get("path-style-access")) {
            credentials.put("path-style-access", false);
        } else {
            credentials.put("path-style-access", true);
        }

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
    public CreateServiceInstanceAppBindingResponse getResponse(Map<String, Object> credentials) {
        CreateServiceInstanceAppBindingResponse.CreateServiceInstanceAppBindingResponseBuilder builder = CreateServiceInstanceAppBindingResponse.builder()
                .credentials(credentials);

        if (volumeMounts != null) {
            builder.volumeMounts(volumeMounts);
        }

        return builder.build();
    }

    private String getS3Url(URL baseUrl, String secretKey, Map<String, Object> parameters) throws IOException {
        String encodedBinding = URLEncoder.encode(this.bindingId, "UTF-8");
        String encodedSecret = URLEncoder.encode(secretKey, "UTF-8");
        String userInfo = encodedBinding + ":" + encodedSecret;
        String s3Url = baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@";

        String portString = "";
        if (baseUrl.getPort() != -1)
            portString = ":" + baseUrl.getPort();

        if (parameters != null && parameters.containsKey("path-style-access") && !(Boolean) parameters.get("path-style-access")) {
            s3Url = s3Url + ecs.prefix(instanceId) + "." + baseUrl.getHost() + portString;
        } else {
            s3Url = s3Url + baseUrl.getHost() + portString + "/" + ecs.prefix(instanceId);
        }
        return s3Url;
    }

    private int createUserMap() throws EcsManagementClientException {
        int unixUid = (int) (2000 + System.currentTimeMillis() % 8000);
        while (true) {
            try {
                ecs.createUserMap(binding.getName(), unixUid);
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

    private List<VolumeMount> createVolumeExport(String export, URL baseUrl, String bucketName, Map<String, Object> parameters) throws EcsManagementClientException {
        int unixUid = createUserMap();
        String host = ecs.getNfsMountHost();
        if (host == null || host.isEmpty()) {
            host = baseUrl.getHost();
        }

        LOG.info("Adding export '{}' to bucket '{}'", export, bucketName);
        String volumeGUID = UUID.randomUUID().toString();
        String absoluteExportPath = ecs.addExportToBucket(bucketName, export);
        LOG.debug("Export added: '{}' for bucket '{}'", export, bucketName);

        Map<String, Object> opts = new HashMap<>();
        String nfsUrl = "nfs://" + host + absoluteExportPath;

        opts.put("source", nfsUrl);
        opts.put("uid", String.valueOf(unixUid));

        List<VolumeMount> mounts = new ArrayList<>();
        mounts.add(new VolumeMount(VOLUME_DRIVER, getContainerDir(parameters, bindingId),
                VolumeMount.Mode.READ_WRITE, VolumeMount.DeviceType.SHARED,
                new SharedVolumeDevice(volumeGUID, opts)));

        return mounts;
    }

    private String getContainerDir(Map<String, Object> parameters, String bindingId) {
        if (parameters != null) {
            Object o = parameters.get(MOUNT);
            if (o instanceof String) {
                String mount = (String) o;
                if (!mount.isEmpty()) {
                    return mount;
                }
            }
        }
        return DEFAULT_MOUNT + File.separator + bindingId;
    }
}
