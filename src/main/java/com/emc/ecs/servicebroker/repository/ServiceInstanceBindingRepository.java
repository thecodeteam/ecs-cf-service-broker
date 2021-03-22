package com.emc.ecs.servicebroker.repository;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.service.s3.S3Service;
import com.emc.ecs.servicebroker.model.Constants;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.bean.ListObjectsResult;
import com.emc.object.s3.bean.S3Object;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.binding.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.binding.VolumeDevice;
import org.springframework.cloud.servicebroker.model.binding.VolumeMount;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@SuppressWarnings("unused")
public class ServiceInstanceBindingRepository {
    static final Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingRepository.class);

    public static final String FILENAME_PREFIX = "service-instance-binding";

    private final ObjectMapper objectMapper = new ObjectMapper();
    {
        // NOTE -- ideally we would not need this code, but for now, the VolumeMount class has
        // custom serialization that is not matched with corresponding deserialization, so
        // deserializing serialized volume mounts doesn't work OOTB.
        SimpleModule module = new SimpleModule();
        module.addDeserializer(VolumeMount.DeviceType.class, new DeviceTypeDeserializer());
        module.addDeserializer(VolumeMount.Mode.class, new ModeDeserializer());
        module.addDeserializer(VolumeDevice.class, new VolumeDeviceDeserializer());
        objectMapper.registerModule(module);
    }

    @Autowired
    private S3Service s3;

    private static String getFilename(String id) {
        return FILENAME_PREFIX + "/" + id + ".json";
    }

    private static boolean isCorrectFilename (String filename) {
        return filename.matches(FILENAME_PREFIX + "/.*\\.json");
    }

    private ServiceInstanceBinding findByFilename(String filename) throws IOException {
        if (!isCorrectFilename(filename)) {
            String errorMessage = format("Invalid filename of service instance binding provided: %s", filename);
            throw new IOException(errorMessage);
        }
        logger.debug("Loading service instance binding from repository file {}", filename);
        GetObjectResult<InputStream> input = s3.getObject(filename);
        return objectMapper.readValue(input.getObject(), ServiceInstanceBinding.class);
    }

    ServiceInstanceBinding removeSecretCredentials(ServiceInstanceBinding binding) {
        Map<String, Object> credentials = binding.getCredentials();
        credentials.remove(Constants.S3_URL);
        credentials.remove(Constants.CREDENTIALS_SECRET_KEY);
        binding.setCredentials(credentials);
        return binding;
    }

    @PostConstruct
    public void initialize() throws EcsManagementClientException {
        logger.info("Service binding file prefix: {}", FILENAME_PREFIX);
    }

    public void save(ServiceInstanceBinding binding) throws IOException {
        String filename = getFilename(binding.getBindingId());
        String serialized = objectMapper.writeValueAsString(binding);
        s3.putObject(filename, serialized);
    }

    public ServiceInstanceBinding find(String id) throws IOException {
        String filename = getFilename(id);
        return findByFilename(filename);
    }

    public ListServiceInstanceBindingsResponse listServiceInstanceBindings(String marker, int pageSize) throws IOException {
        if (pageSize < 0) {
            throw new IOException("Page size could not be negative number");
        }
        List<ServiceInstanceBinding> bindings = new ArrayList<>();
        ListObjectsResult list = marker != null ?
                s3.listObjects(FILENAME_PREFIX + "/", getFilename(marker), pageSize) :
                s3.listObjects(FILENAME_PREFIX + "/", null, pageSize);
        for (S3Object s3Object: list.getObjects()) {
            String filename = s3Object.getKey();
            if (isCorrectFilename(filename)) {
                ServiceInstanceBinding binding = findByFilename(filename);
                bindings.add(removeSecretCredentials(binding));
            }
        }
        ListServiceInstanceBindingsResponse response = new ListServiceInstanceBindingsResponse(bindings);
        response.setMarker(list.getMarker());
        response.setPageSize(list.getMaxKeys());
        response.setNextMarker(list.getNextMarker());
        return response;
    }

    public void delete(String id) {
        String filename = getFilename(id);
        s3.deleteObject(filename);
    }

    public static class ModeDeserializer extends StdDeserializer<VolumeMount.Mode> {

        ModeDeserializer() {
            this(null);
        }

        ModeDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public VolumeMount.Mode deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            String s = node.asText();
            if (s.equals("rw")) {
                return VolumeMount.Mode.READ_WRITE;
            } else {
                return VolumeMount.Mode.READ_ONLY;
            }
        }
    }

    public static class DeviceTypeDeserializer extends StdDeserializer<VolumeMount.DeviceType> {

        DeviceTypeDeserializer() {
            this(null);
        }

        DeviceTypeDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public VolumeMount.DeviceType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return VolumeMount.DeviceType.SHARED;
        }
    }

    public static class VolumeDeviceDeserializer extends StdDeserializer<VolumeDevice> {

        VolumeDeviceDeserializer() {
            this(null);
        }

        VolumeDeviceDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public VolumeDevice deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return jp.getCodec().readValue(jp, SharedVolumeDevice.class);
        }
    }

}