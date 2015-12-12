package com.emc.ecs.managementClient.model;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_secret_keys")
public class UserSecretKeyList {
	
	private String secretKey1;
	private String keyTimestamp1;
	private String secretKey2;
	private String keyTimestamp2;
	
	@XmlElement(name = "secret_key_1")
	public String getSecretKey1() {
		return secretKey1;
	}
	public void setSecretKey1(String secretKey1) {
		this.secretKey1 = secretKey1;
	}

	@XmlElement(name = "key_timestamp_1")
	public String getKeyTimestamp1() {
		return keyTimestamp1;
	}
	public void setKeyTimestamp1(String keyTimestamp1) {
		this.keyTimestamp1 = keyTimestamp1;
	}

	@XmlElement(name = "secret_key_2")	
	public String getSecretKey2() {
		return secretKey2;
	}

	public void setSecretKey2(String secretKey2) {
		this.secretKey2 = secretKey2;
	}

	@XmlElement(name = "key_timestamp_2")
	public String getKeyTimestamp2() {
		return keyTimestamp2;
	}
	
	public void setKeyTimestamp2(String keyTimestamp2) {
		this.keyTimestamp2 = keyTimestamp2;
	}

	public List<UserSecretKey> asList() {
		UserSecretKey key1 = new UserSecretKey();
		key1.setSecretKey(secretKey1);
		key1.setKeyTimestamp(keyTimestamp1);
		
		UserSecretKey key2 = new UserSecretKey();
		key2.setKeyTimestamp(keyTimestamp2);
		key2.setKeyTimestamp(keyTimestamp2);

		return Arrays.asList(key1, key2);
	}
	
}
