package com.emc.ecs.serviceBroker.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.springframework.beans.factory.annotation.Autowired;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;

public class ServiceInstanceBindingRepository {
	
	S3JerseyClient s3;
	String bucket;

	@Autowired
	public ServiceInstanceBindingRepository(EcsService ecs)
			throws EcsManagementClientException, EcsManagementResourceNotFoundException, URISyntaxException {
		super();
		EcsRepositoryCredentials creds = ecs.getCredentials();
		String endpoint = ecs.getObjectEndpoint();
		S3Config s3Config = new S3Config(new URI(endpoint));
		s3Config.withIdentity(creds.getUserName()).withSecretKey(creds.getUserSecret());
		this.s3 = new S3JerseyClient(s3Config);
		this.bucket = creds.getBucketName();
	}

	public void save(ServiceInstanceBinding binding) throws IOException, JAXBException {
		PipedInputStream inStream = new PipedInputStream();
		PipedOutputStream outStream = new PipedOutputStream(inStream);
		JAXBContext jaxbContext = JAXBContext.newInstance(ServiceInstance.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.marshal(binding, outStream);
		outStream.close();
		s3.putObject(bucket, getFilename(binding.getId()), inStream, null);
	}

	public ServiceInstanceBinding find(String id) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ServiceInstanceBinding.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		GetObjectResult<InputStream> outStream = s3.getObject(bucket, getFilename(id));
		return (ServiceInstanceBinding) jaxbUnmarshaller.unmarshal(outStream.getObject());
	}

	public void delete(String id) {
		s3.deleteObject(bucket, getFilename(id));
	}
	
	private String getFilename(String id) {
		return "service-instance-binding/" + id + ".xml";
	}

}
