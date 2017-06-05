package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.CatalogConfig;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBinding;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.management.sdk.model.UserSecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EcsServiceInstanceBindingService
        implements ServiceInstanceBindingService {
    private static final String NO_SERVICE_MATCHING_TYPE = "No service matching type: ";
    private static final String SERVICE_TYPE = "service-type";
    private static final String NAMESPACE = "namespace";
    private static final String BUCKET = "bucket";
    private static final String VOLUME_DRIVER = "nfsv3driver";
    private static final String DEFAULT_CONTAINER_DIR = "/var/vcap/data";

    static final Logger LOG = LoggerFactory.getLogger(EcsServiceInstanceBindingService.class);

    @Autowired
    private EcsService ecs;

    @Autowired
    private CatalogConfig catalog;

    @Autowired
    private ServiceInstanceBindingRepository repository;

    public EcsServiceInstanceBindingService()
            throws EcsManagementClientException,
            EcsManagementResourceNotFoundException, URISyntaxException {
        super();
    }

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {
        LOG.error("Creating service instance binding response");
        String instanceId = request.getServiceInstanceId();
        String bindingId = request.getBindingId();
        String serviceDefinitionId = request.getServiceDefinitionId();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(request);
        Map<String, Object> credentials = new HashMap<>();
        Map<String, Object> parameters = request.getParameters();
        credentials.put("accessKey", ecs.prefix(bindingId));
        try {
            if (ecs.userExists(bindingId))
                throw new ServiceInstanceBindingExistsException(instanceId,
                        bindingId);
            binding.setBindingId(bindingId);

            ServiceDefinitionProxy service = ecs
                    .lookupServiceDefinition(serviceDefinitionId);
            PlanProxy plan = service.findPlan(request.getPlanId());

            String serviceType = (String) service.getServiceSettings()
                    .get(SERVICE_TYPE);

            UserSecretKey userSecret;
            CreateServiceInstanceAppBindingResponse resp =  new CreateServiceInstanceAppBindingResponse();
            String s3Url;
            String endpoint;

            if (NAMESPACE.equals(serviceType)) {
                userSecret = ecs.createUser(bindingId, instanceId);
                LOG.info("Created user with binding id: " + bindingId + ", instanceId: " + instanceId);
                endpoint = ecs.getNamespaceURL(ecs.prefix(instanceId), service, plan,
                        Optional.ofNullable(parameters));
                String userInfo = bindingId + ":" + userSecret.getSecretKey();
                URL baseUrl = new URL(endpoint);
                s3Url = new StringBuilder(baseUrl.getProtocol())
                        .append("://")
                        .append(ecs.prefix(userInfo))
                        .append("@")
                        .append(baseUrl.getHost())
                        .append(":")
                        .append(baseUrl.getPort())
                        .toString();
            } else if (BUCKET.equals(serviceType)) {
                endpoint = ecs.getObjectEndpoint();
                URL baseUrl = new URL(endpoint);
                userSecret = ecs.createUser(bindingId);
                LOG.info("Created user with binding id: " + bindingId);
                List<String> permissions = null;
                Boolean hasMounts = ecs.getBucketFileEnabled(instanceId);
                String export = "";
                if (parameters != null) {
                    permissions = (List<String>) parameters.get("permissions");
                    export = (String) parameters.get("export");
                }
                if (permissions != null) {
                    ecs.addUserToBucket(instanceId, bindingId, permissions);
                    LOG.info("Added user to bucket");
                } else {
                    ecs.addUserToBucket(instanceId, bindingId);
                    LOG.info("Added user to bucket");
                }
                if (hasMounts) {
                    int unixUid = (int)(2000 + System.currentTimeMillis() % 8000);
                    while (true) {
                        try {
                            ecs.createUserMap(bindingId, unixUid);
                            LOG.info("Created user map binding id: " + bindingId + ", unixUid: " + unixUid);
                            break;
                        } catch (EcsManagementClientException e) {
                            if (e.getMessage().contains("Bad request body (1013)")) {
                                unixUid++;
                            } else {
                                throw e;
                            }
                        }
                    }

                    String host = ecs.getNfsMountHost();
                    if (host == null || host.isEmpty()) {
                        host = baseUrl.getHost();
                    }
                    String volumeGUID = UUID.randomUUID().toString();
                    String absoluteExportPath = ecs.addExportToBucket(instanceId, export);
                    Map<String, Object> opts = new HashMap<>();
                    String nfsUrl = new StringBuilder("nfs://")
                                .append(host)
                                .append(absoluteExportPath)
                                .toString();
                    opts.put("source", nfsUrl);
                    opts.put("uid", String.valueOf(unixUid));
                    List<VolumeMount> mounts = new ArrayList<>();
                    mounts.add(new VolumeMount(VOLUME_DRIVER, DEFAULT_CONTAINER_DIR, VolumeMount.Mode.READ_WRITE,
                            VolumeMount.DeviceType.SHARED, new SharedVolumeDevice(volumeGUID, opts)));
                    binding.setVolumeMounts(mounts);
                    resp = resp.withVolumeMounts(mounts);
                }
                credentials.put("bucket", ecs.prefix(instanceId));
                String userInfo = bindingId + ":" + userSecret.getSecretKey();
                s3Url = new StringBuilder(baseUrl.getProtocol())
                            .append("://")
                            .append(ecs.prefix(userInfo))
                            .append("@")
                            .append(baseUrl.getHost())
                            .append(":")
                            .append(baseUrl.getPort())
                            .append("/")
                            .append(ecs.prefix(instanceId))
                            .toString();
            } else {
                throw new EcsManagementClientException(
                        NO_SERVICE_MATCHING_TYPE + serviceType);
            }

            credentials.put("s3Url", s3Url);
            credentials.put("endpoint", endpoint);
            credentials.put("secretKey", userSecret.getSecretKey());
            binding.setCredentials(credentials);

            repository.save(binding);
            return resp.withCredentials(credentials);

        } catch (IOException | JAXBException | EcsManagementClientException e) {
            throw new ServiceBrokerException(e);
        }
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request)
            throws ServiceBrokerException {
        String bindingId = request.getBindingId();
        String instanceId = request.getServiceInstanceId();
        String serviceDefinitionId = request.getServiceDefinitionId();
        ServiceDefinitionProxy service = catalog
                .findServiceDefinition(serviceDefinitionId);
        String serviceType = (String) service.getServiceSettings()
                .get(SERVICE_TYPE);
        try {
            if (BUCKET.equals(serviceType))
                ecs.removeUserFromBucket(instanceId, bindingId);
            ecs.deleteUser(bindingId);
            LOG.error("looking up binding: " + bindingId);
            ServiceInstanceBinding binding = repository.find(bindingId);
            if (binding == null) {
                // TODO -- is this gonna blow up?
                repository.delete(bindingId);
                return;
            }
            LOG.error("binding found: " + bindingId);

            List<VolumeMount> volumes = binding.getVolumeMounts();
            if (volumes == null || volumes.size() == 0) {
                repository.delete(bindingId);
                return;
            }

            Map<String, Object> mountConfig = ((SharedVolumeDevice) volumes.get(0).getDevice()).getMountConfig();
            String unixId = (String) mountConfig.get("uid");
            try {
                LOG.error("Deleting user map of instance Id and Binding Id " + instanceId + " " + bindingId);
                ecs.deleteUserMap(bindingId, unixId);
            } catch (EcsManagementClientException e) {
                LOG.error("Error deleting user map: " + e.getMessage());
            }

            repository.delete(bindingId);
        } catch (Exception e) {
            LOG.error("Error deleting binding: " + e);
            throw new ServiceBrokerException(e);
        }
    }
}
