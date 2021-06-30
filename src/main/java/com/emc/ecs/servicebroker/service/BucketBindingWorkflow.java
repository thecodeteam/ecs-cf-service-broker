package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class BucketBindingWorkflow extends BindingWorkflowImpl {
    private static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    private List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();

    BucketBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) throws IOException {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        ServiceInstance serviceInstance = getInstance();
        String namespace = (String) serviceInstance.getServiceSettings().getOrDefault(NAMESPACE, ecs.getDefaultNamespace());
        if (ecs.userExists(binding.getName(), namespace))
            throw new ServiceInstanceBindingExistsException(serviceInstance.getServiceInstanceId(), bindingId);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException, JAXBException {
        ServiceInstance serviceInstance = getInstance();
        String namespace = (String) serviceInstance.getServiceSettings().getOrDefault(NAMESPACE, ecs.getDefaultNamespace());
        String bucket = serviceInstance.getName();

        UserSecretKey userSecretKey = ecs.createUser(binding.getName(), namespace);

        Map<String, Object> parameters = createRequest.getParameters();

        String export = "";
        List<String> permissions = null;
        if (parameters != null) {
            permissions = (List<String>) parameters.get(USER_PERMISSIONS);
            export = (String) parameters.getOrDefault(VOLUME_EXPORT, null);
        }

        if (permissions == null) {
            ecs.addUserToBucket(bucket, namespace, binding.getName());
        } else {
            ecs.addUserToBucket(bucket, namespace, binding.getName(), permissions);
        }

        if (ecs.getBucketFileEnabled(bucket, namespace)) {
            volumeMounts = createVolumeExport(export, new URL(ecs.getObjectEndpoint()), bucket, namespace, parameters);
        }

        return userSecretKey.getSecretKey();
    }

    @Override
    public void removeBinding() throws EcsManagementClientException, IOException {
        ServiceInstance instance = getInstance();
        String namespace = (String) instance.getServiceSettings().getOrDefault("namespace", ecs.getDefaultNamespace());
        String bucket = instance.getName();

        List<VolumeMount> volumes = binding.getVolumeMounts();

        if (volumes != null && volumes.size() > 0) {
            Map<String, Object> mountConfig = ((SharedVolumeDevice) volumes.get(0).getDevice()).getMountConfig();
            String unixId = (String) mountConfig.get("uid");

            LOG.info("Deleting user map for bucket '{}' in namespace '{}', binding '{}' ", bucket, namespace, bindingId);

            try {
                ecs.deleteUserMap(binding.getName(), namespace, unixId);
            } catch (EcsManagementClientException e) {
                LOG.error("Error deleting user map: " + e.getMessage());
            }
        }

        LOG.info("Removing binding user '{}' from bucket '{}' in namespace '{}'", bindingId, bucket, namespace);

        ecs.removeUserFromBucket(bucket, namespace, binding.getName());
        ecs.deleteUser(binding.getName(), namespace);
    }

    @Override
    public Map<String, Object> getCredentials(String secretKey, Map<String, Object> parameters) throws IOException, EcsManagementClientException {
        ServiceInstance instance = getInstance();
        String namespace = (String) instance.getServiceSettings().getOrDefault("namespace", ecs.getDefaultNamespace());

        // S3 path style access is taken from broker level configuration (when no value passed through parameters)
        Map<String, Object> brokerConfig = ecs.getBrokerConfig();
        boolean pathStyleAccess = brokerConfig == null || (boolean) brokerConfig.getOrDefault(PATH_STYLE_ACCESS, true);
        if (parameters != null && parameters.containsKey(PATH_STYLE_ACCESS)) {
            pathStyleAccess = (Boolean) parameters.get(PATH_STYLE_ACCESS);
        }

        String endpoint = ecs.getObjectEndpoint();

        String bucketName = ecs.prefix(instance.getName());

        LOG.info("Generating {}-style S3 path for instance '{}'", (pathStyleAccess ? "path" : "domain"), instance.getServiceInstanceId());
        String s3Url = buildS3Url(bucketName, endpoint, secretKey, pathStyleAccess);

        Map<String, Object> credentials = super.getCredentials(secretKey);
        credentials.put(ENDPOINT, endpoint);                        // Add default broker endpoint
        credentials.put(PATH_STYLE_ACCESS, pathStyleAccess);
        credentials.put(S3_URL, s3Url);                             // Add S3 URL
        credentials.put(BUCKET, bucketName);    // Add bucket name from repository
        credentials.put(NAMESPACE, namespace);

        if(!volumeMounts.isEmpty()) {
            VolumeMount volumeMount = volumeMounts.get(volumeMounts.size() - 1);
            SharedVolumeDevice volumeDevice = (SharedVolumeDevice) volumeMount.getDevice();
            Map<String, Object> config = volumeDevice.getMountConfig();
            int unixID = Integer.parseInt((String)config.get(VOLUME_EXPORT_UID));
            credentials.put(VOLUME_EXPORT_UID, unixID);
        }

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

    private String buildS3Url(String prefixedBucketName, String endpoint, String secretKey, boolean usePathStyleS3) throws IOException {
        String encodedBinding = URLEncoder.encode(binding.getName(), "UTF-8");
        String encodedSecret = URLEncoder.encode(secretKey, "UTF-8");

        String prefixedUserInfo = ecs.prefix(encodedBinding + ":" + encodedSecret);
        String prefixedInstanceId = prefixedBucketName;

        URL baseUrl = new URL(endpoint);

        String port = "";
        if (baseUrl.getPort() != -1) {
            port = ":" + baseUrl.getPort();
        }

        if (usePathStyleS3) {
            return baseUrl.getProtocol() + "://" + prefixedUserInfo + "@" + baseUrl.getHost() + port + "/" + prefixedInstanceId;
        } else {
            return baseUrl.getProtocol() + "://" + prefixedUserInfo + "@" + prefixedInstanceId + "." + baseUrl.getHost() + port;
        }
    }

    private int createUserMap(String namespace) throws EcsManagementClientException {
        int unixUid = (int) (2000 + System.currentTimeMillis() % 8000);
        while (true) {
            try {
                ecs.createUserMap(binding.getName(), namespace, unixUid);
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

    private List<VolumeMount> createVolumeExport(String export, URL baseUrl, String bucketName, String namespace, Map<String, Object> parameters) throws EcsManagementClientException {
        int unixUid = createUserMap(namespace);
        String host = ecs.getNfsMountHost();
        if (host == null || host.isEmpty()) {
            host = baseUrl.getHost();
        }

        LOG.info("Adding export '{}' to bucket '{}' in namespace '{}'", export, bucketName, namespace);
        String absoluteExportPath = ecs.addExportToBucket(bucketName, namespace, export);
        LOG.debug("Export added: '{}' for bucket '{}'", export, bucketName);

        Map<String, Object> opts = new HashMap<>();
        opts.put(VOLUME_EXPORT_SOURCE, "nfs://" + host + absoluteExportPath);
        opts.put(VOLUME_EXPORT_UID, String.valueOf(unixUid));

        String volumeGUID = UUID.randomUUID().toString();

        List<VolumeMount> mounts = new ArrayList<>();
        mounts.add(new VolumeMount(VOLUME_DRIVER, getContainerDir(parameters, bindingId),
                VolumeMount.Mode.READ_WRITE, VolumeMount.DeviceType.SHARED,
                new SharedVolumeDevice(volumeGUID, opts)));

        return mounts;
    }

    private String getContainerDir(Map<String, Object> parameters, String bindingId) {
        if (parameters != null) {
            Object o = parameters.get(VOLUME_MOUNT);
            if (o instanceof String) {
                String mount = (String) o;
                if (!mount.isEmpty()) {
                    return mount;
                }
            }
        }
        return VOLUME_DEFAULT_MOUNT + File.separator + bindingId;
    }
}
