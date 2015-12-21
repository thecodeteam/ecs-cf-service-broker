package com.emc.ecs.serviceBroker.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceInstanceRepository {
	
	String bucket;
	S3JerseyClient s3;
	ObjectMapper objectMapper = new ObjectMapper();
		
	@Autowired
	public ServiceInstanceRepository(EcsService ecs)
			throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		super();
		EcsRepositoryCredentials creds = ecs.getCredentials();
		S3Config s3Config = new S3Config(new URI(creds.getEndpoint()));
		s3Config.withIdentity(creds.getPrefixedUserName()).withSecretKey(creds.getUserSecret());
		this.s3 = new S3JerseyClient(s3Config);
		this.bucket = creds.getPrefixedBucketName();
	}

	public void save(ServiceInstance instance) throws IOException, JAXBException {
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream output = new PipedOutputStream(input);
		objectMapper.writeValue(output, instance);
		output.close();
		s3.putObject(bucket, getFilename(instance.getId()), input, null);
	}

	public ServiceInstance find(String id) throws JsonParseException, JsonMappingException, IOException {
		GetObjectResult<InputStream> input = s3.getObject(bucket, getFilename(id));
		return (ServiceInstance) objectMapper.readValue(input.getObject(), ServiceInstance.class);
	}

	public void delete(String id) {
		s3.deleteObject(bucket, getFilename(id));
	}
	
	private String getFilename(String id) {
		return "service-instance/" + id + ".json";
	}

}