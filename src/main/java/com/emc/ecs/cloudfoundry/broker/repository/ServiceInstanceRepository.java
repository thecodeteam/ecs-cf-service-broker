package com.emc.ecs.cloudfoundry.broker.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.config.BrokerConfig;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceInstanceRepository {

	private String bucket;
	private S3JerseyClient s3;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private BrokerConfig broker;

	
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

	public void save(ServiceInstance instance)
			throws IOException, JAXBException {
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream output = new PipedOutputStream(input);
		objectMapper.writeValue(output, instance);
		output.close();
		s3.putObject(bucket, getFilename(instance.getServiceInstanceId()), input, null);
	}

	public ServiceInstance find(String id)
			throws IOException {
		GetObjectResult<InputStream> input = s3.getObject(bucket,
				getFilename(id));
		return objectMapper.readValue(input.getObject(),
				ServiceInstance.class);
	}

	public void delete(String id) {
		s3.deleteObject(bucket, getFilename(id));
	}

	private String getFilename(String id) {
		return "service-instance/" + id + ".json";
	}

}