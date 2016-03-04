package com.emc.ecs.serviceBroker.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.config.BrokerConfig;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceInstanceBindingRepository {

	private S3JerseyClient s3;
	private String bucket;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	BrokerConfig broker;
	
	@PostConstruct
	public void initialize()
			throws EcsManagementClientException,
			EcsManagementResourceNotFoundException, URISyntaxException {
		S3Config s3Config = new S3Config(
				new URI(broker.getRepositoryEndpoint()));
		s3Config.withIdentity(broker.getPrefixedUserName())
				.withSecretKey(broker.getRepositorySecret());
		this.s3 = new S3JerseyClient(s3Config);
		this.bucket = broker.getPrefixedBucketName();
	}

	public void save(ServiceInstanceBinding binding)
			throws IOException, JAXBException {
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream output = new PipedOutputStream(input);
		objectMapper.writeValue(output, binding);
		output.close();
		s3.putObject(bucket, getFilename(binding.getBindingId()), input, null);
	}

	public ServiceInstanceBinding find(String id)
			throws JsonParseException, JsonMappingException, IOException {
		GetObjectResult<InputStream> input = s3.getObject(bucket,
				getFilename(id));
		ServiceInstanceBinding binding = objectMapper.readValue(
				input.getObject(), ServiceInstanceBinding.class);
		return binding;
	}

	public void delete(String id) {
		s3.deleteObject(bucket, getFilename(id));
	}

	private String getFilename(String id) {
		return "service-instance-binding/" + id + ".json";
	}

}