package com.emc.ecs.cloudfoundry.broker.repository;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.BrokerConfig;
import com.emc.ecs.management.sdk.ObjectUserMapAction;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.SharedVolumeDevice;
import org.springframework.cloud.servicebroker.model.VolumeDevice;
import org.springframework.cloud.servicebroker.model.VolumeMount;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
public class ServiceInstanceBindingRepository {

    static final Logger LOG = LoggerFactory.getLogger(ObjectUserMapAction.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private S3JerseyClient s3;
    private String bucket;
    @Autowired
    private BrokerConfig broker;

    private static String getFilename(String id) {
        return "service-instance-binding/" + id + ".json";
    }

    @PostConstruct
    public void initialize() throws EcsManagementClientException,
            EcsManagementResourceNotFoundException, URISyntaxException {
        S3Config s3Config = new S3Config(
                new URI(broker.getRepositoryEndpoint()));
        s3Config.withIdentity(broker.getPrefixedUserName())
                .withSecretKey(broker.getRepositorySecret());
        this.s3 = new S3JerseyClient(s3Config);
        this.bucket = broker.getPrefixedBucketName();

        // NOTE -- ideally we would not need this code, but for now, the VolumeMount class has
        // custom serialization that is not matched with corresponding deserialization, so
        // deserializing serialized volume mounts doesn't work OOTB.
        SimpleModule module = new SimpleModule();
        module.addDeserializer(VolumeMount.DeviceType.class, new DeviceTypeDeserializer());
        module.addDeserializer(VolumeMount.Mode.class, new ModeDeserializer());
        module.addDeserializer(VolumeDevice.class, new VolumeDeviceDeserializer());
        objectMapper.registerModule(module);
    }

    public void save(ServiceInstanceBinding binding)
            throws IOException, JAXBException {
        PipedInputStream input = new PipedInputStream();
        PipedOutputStream output = new PipedOutputStream(input);
        objectMapper.writeValue(output, binding);
        output.close();
        s3.putObject(bucket, getFilename(binding.getBindingId()), input, null);
    }

    public ServiceInstanceBinding find(String id) throws IOException {
        GetObjectResult<InputStream> input = s3.getObject(bucket,
                getFilename(id));
        return objectMapper.readValue(input.getObject(),
                ServiceInstanceBinding.class);
    }

    public void delete(String id) {
        s3.deleteObject(bucket, getFilename(id));
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
            LOG.error("trying to unmarshall volume mount");
            SharedVolumeDevice s = jp.getCodec().readValue(jp, SharedVolumeDevice.class);
            LOG.error("unmarshalled volume mount: " + s.getVolumeId());

            return s;
        }
    }

}