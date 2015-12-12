package com.emc.ecs.managementClient.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_secret_key_create")
public class UserSecretKeyCreate {
	
	private String existingKeyExpiryTimeMins;
	private String namespace;
	private String secretKey;
	
	public UserSecretKeyCreate() {
		super();
	}
	
	public UserSecretKeyCreate(String secretKey) {
		super();
		this.secretKey = secretKey;
	}

	@XmlElement(name = "existing_key_expiry_time_mins")
	public String getExistingKeyExpiryTimeMins() {
		return existingKeyExpiryTimeMins;
	}
	
	public void setExistingKeyExpiryTimeMins(String existingKeyExpiryTimeMins) {
		this.existingKeyExpiryTimeMins = existingKeyExpiryTimeMins;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	@XmlElement(name = "secret_key")
	public String getSecretKey() {
		return secretKey;
	}
	
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	

}
