package com.emc.ecs.serviceBroker;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.emc.ecs.serviceBroker.model.ObjectBucket;
import com.emc.ecs.serviceBroker.model.UserCreateParam;
import com.emc.ecs.serviceBroker.model.UserDeleteParam;
import com.emc.ecs.serviceBroker.model.UserSecretKey;
import com.emc.ecs.serviceBroker.model.UserSecretKeyCreate;

public class EcsManagementClient {

	private String managementEndpoint;
	private String adminUsername;
	private String adminPassword;
	private String authToken;
	private String namespace;
	private String vpool;
	
	public EcsManagementClient(String managementEndpoint, String adminUsername, String adminPassword,
			String namespace, String vpool) {
		super();
		this.managementEndpoint = managementEndpoint;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.namespace = namespace;
		this.vpool = vpool;
	}

	public String getManagementEndpoint() {
		return managementEndpoint;
	}

	public void setManagementEndpoint(String managementEndpoint) {
		this.managementEndpoint = managementEndpoint;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getVpool() {
		return vpool;
	}

	public void setVpool(String vpool) {
		this.vpool = vpool;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}	
	
	private Client buildJerseyClient() {
		/**
		 * Disable host name verification. Should be able to configure the
		 * ECS certificate with the correct host name to avoid this.
		 **/
		HostnameVerifier hostnameVerifier = getHostnameVerifier();
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return JerseyClientBuilder.newBuilder()
				.register(hostnameVerifier)
				.build();
	}
		
	private HostnameVerifier getHostnameVerifier() {
		return new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
	}

	public boolean isLoggedIn() {
		return authToken != null;
	}

	public void login() throws EcsManagementClientException {
		UriBuilder uriBuilder = UriBuilder.fromPath(managementEndpoint)
				.segment("login");
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basicBuilder()
			      .credentials(adminUsername, adminPassword)
			      .build();
		Client jerseyClient = buildJerseyClient().register(authFeature);
		
		Response response = jerseyClient.target(uriBuilder)
				.request()
				.get();
		try {
			handleResponse(response);			
		} catch(EcsManagementResourceNotFoundException e) {
			throw new EcsManagementClientException(e.getMessage());
		}
		this.authToken = response.getHeaderString("X-SDS-AUTH-TOKEN");
	}

	public String getAuthToken() {
		return authToken;
	}

	public void createBucket(String id) {
		// TODO Auto-generated method stub
	}

	public boolean bucketExists(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public ObjectBucket getBucket(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteBucket(String id) {
		// TODO Auto-generated method stub
		
	}

	public void createObjectUser(String username) throws EcsManagementClientException {
		UriBuilder uri = UriBuilder.fromPath(managementEndpoint)
				.segment("object", "users");
		handleRemoteCall("post", uri, new UserCreateParam(username, namespace));
	}

	public Boolean userExists(String username) throws EcsManagementClientException {
		UriBuilder uri = UriBuilder.fromPath(managementEndpoint)
				.segment("object", "users", username, "info");
		Response response = makeRemoteCall("get", uri, null);
		try {
			handleResponse(response);			
		} catch (EcsManagementResourceNotFoundException e) {
			return false;			
		}
		return true;
	}

	public void deleteObjectUser(String username) throws EcsManagementClientException {
		UriBuilder uri = UriBuilder.fromPath(managementEndpoint)
				.segment("object", "users", "deactivate");
		handleRemoteCall("post", uri, new UserDeleteParam(username));
	}

	public UserSecretKey createUserSecretKey(String username) throws EcsManagementClientException {
		UriBuilder uri = UriBuilder.fromPath(managementEndpoint)
				.segment("object", "user-secret-keys", username);
		Response response = handleRemoteCall("post", uri, new UserSecretKeyCreate());
		return response.readEntity(UserSecretKey.class);
	}

	public void applyBucketQuota(String id, int limit, int warn) {
		// TODO Auto-generated method stub
		
	}

	public void removeBucketQuota(String id) {
		// TODO Auto-generated method stub
		
	}
	
	public void applyBucketUserAcl(String bucket, String username, String permission) {
		// TODO Auto-generated method stub
		
	}

	public void removeBucketUserAcl(String bucket, String username) {
		// TODO Auto-generated method stub
		
	}
	
	private Response makeRemoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException {
		if (! isLoggedIn())
			login();
		Client jerseyClient = buildJerseyClient();
		Builder request = jerseyClient.target(uri)
				.request()
				.header("X-SDS-AUTH-TOKEN", authToken)
				.header("Accept", "application/xml");
		Response response = null;
		if (method == "get") {
			response = request.get();
		} else if (method == "post") {
			response = request.post(Entity.xml(arg));
		} else if (method == "put") {
			response = request.put(Entity.xml(arg));
		}
		return response;
	}
	
	private Response handleRemoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException {
		Response response = makeRemoteCall(method, uri, arg);
		try {			
			handleResponse(response);			
		} catch (EcsManagementResourceNotFoundException e) {
			throw new EcsManagementClientException(e.getMessage());
		}
		return response;
	}
	
	private void handleResponse(Response response) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		if (response.getStatus() == 404) {
			throw new EcsManagementResourceNotFoundException(response.getStatusInfo().toString());
		} else if (response.getStatus() > 399) {
			String message = response.readEntity(String.class);
			System.out.println("Error: " + message);
			throw new EcsManagementClientException(response.getStatusInfo().toString()
					+ ": " + message);
		}
	}

}