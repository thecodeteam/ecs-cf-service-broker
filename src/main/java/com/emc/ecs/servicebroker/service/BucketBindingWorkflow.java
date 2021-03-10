package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.UserSecretKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.*;

public class BucketBindingWorkflow extends BindingWorkflowImpl {
    private static final Logger LOG = LoggerFactory.getLogger(BucketBindingWorkflow.class);

    private static final Long TWO_THOUSAND = 2000L;
    private static final Long EIGHT_THOUSAND = 8000L;

    private static final String ERROR_DELETING_USERMAP = "Error deleting user map: ";

    private List<VolumeMount> volumeMounts = new ArrayList<>();

    BucketBindingWorkflow(ServiceInstanceRepository instanceRepo, EcsService ecs) {
        super(instanceRepo, ecs);
    }

    public void checkIfUserExists() throws EcsManagementClientException, IOException {
        ServiceInstance serviceInstance = instanceRepository.find(instanceId);
        String namespace = (String) serviceInstance.getServiceSettings().getOrDefault(NAMESPACE, ecs.getDefaultNamespace());
        if (ecs.doesUserExist(binding.getName(), namespace))
            throw new ServiceInstanceBindingExistsException(instanceId, bindingId);
    }

    @Override
    public String createBindingUser() throws EcsManagementClientException, IOException {
        ServiceInstance instance = getInstance();
        String namespace = (String) instance.getServiceSettings().getOrDefault(NAMESPACE, ecs.getDefaultNamespace());
        String bucket = instance.getName();

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

        if (volumes != null && !volumes.isEmpty()) {
            Map<String, Object> mountConfig = ((SharedVolumeDevice) volumes.get(0).getDevice()).getMountConfig();
            String unixId = (String) mountConfig.get("uid");

            LOG.info("Deleting user map for bucket '{}' in namespace '{}', binding '{}' ", bucket, namespace, bindingId);

            try {
                ecs.deleteUserMap(binding.getName(), namespace, unixId);
            } catch (EcsManagementClientException e) {
                LOG.error(ERROR_DELETING_USERMAP + e.getMessage());
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
        String bucket = instance.getName();

        Map<String, Object> credentials = getCredentials(secretKey);

        // Add default broker endpoint
        String endpoint = ecs.getObjectEndpoint();
        credentials.put(ENDPOINT, endpoint);

        // Add s3 URL
        URL baseUrl = new URL(endpoint);
        credentials.put(S3_URL, getS3Url(baseUrl, secretKey, parameters));

        if (parameters != null && parameters.containsKey(PATH_STYLE_ACCESS) && !(Boolean) parameters.get(PATH_STYLE_ACCESS)) {
            credentials.put(PATH_STYLE_ACCESS, false);
        } else {
            credentials.put(PATH_STYLE_ACCESS, true);
        }

        // Add bucket name from repository
        credentials.put(BUCKET, ecs.prefix(bucket));
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

    private String getS3Url(URL baseUrl, String secretKey, Map<String, Object> parameters) throws IOException {
        String encodedBinding = URLEncoder.encode(bindingId, "UTF-8");
        String encodedSecret = URLEncoder.encode(secretKey, "UTF-8");
        String userInfo = encodedBinding + ":" + encodedSecret;
        String s3Url = baseUrl.getProtocol() + "://" + ecs.prefix(userInfo) + "@";

        String portString = "";
        if (baseUrl.getPort() != -1)
            portString = ":" + baseUrl.getPort();

        if (parameters != null && parameters.containsKey(PATH_STYLE_ACCESS) && !(Boolean) parameters.get(PATH_STYLE_ACCESS)) {
            s3Url = s3Url + ecs.prefix(instanceId) + "." + baseUrl.getHost() + portString;
        } else {
            s3Url = s3Url + baseUrl.getHost() + portString + "/" + ecs.prefix(instanceId);
        }
        return s3Url;
    }

    private int createUserMap(String namespace) throws EcsManagementClientException {
        int unixUid = (int) (TWO_THOUSAND + System.currentTimeMillis() % EIGHT_THOUSAND);
        while (true) {
            try {
                ecs.createUserMap(binding.getName(), namespace, unixUid);
                return unixUid;
            } catch (EcsManagementClientException e) {
                if (e.getMessage().contains("Bad request body (1013)")) {
                    unixUid++;
                } else {
                    throw e;
                }
            }
        }
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

        Map<String, Object> opts = new HashMap<>(2);
        opts.put(VOLUME_EXPORT_SOURCE, "nfs://" + host + absoluteExportPath);
        opts.put(VOLUME_EXPORT_UID, String.valueOf(unixUid));

        String volumeGUID = UUID.randomUUID().toString();

        List<VolumeMount> mounts = new ArrayList<>(1);
        mounts.add(new VolumeMount(VOLUME_DRIVER, getContainerDir(parameters, bindingId),
                VolumeMount.Mode.READ_WRITE, VolumeMount.DeviceType.SHARED,
                new SharedVolumeDevice(volumeGUID, opts)));

        return mounts;
    }

    private static String getContainerDir(Map<String, Object> parameters, String bindingId) {
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


    private ServiceInstance getInstance() throws IOException {
        ServiceInstance instance = instanceRepository.find(instanceId);
        if (instance == null)
            throw new ServiceInstanceDoesNotExistException(instanceId);

        if (instance.getName() == null)
            instance.setName(instance.getServiceInstanceId());

        return instance;
    }
}
