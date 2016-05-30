package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_secret_key")
public class UserSecretKey {

    private String keyExpiryTimestamp;
    private String secretKey;
    private String keyTimestamp;
    private Link link;

    @XmlElement(name = "secret_key")
    public String getSecretKey() {
	return secretKey;
    }

    public void setSecretKey(String secretKey) {
	this.secretKey = secretKey;
    }

    @XmlElement(name = "key_timestamp")
    public String getKeyTimestamp() {
	return keyTimestamp;
    }

    public void setKeyTimestamp(String keyTimestamp) {
	this.keyTimestamp = keyTimestamp;
    }

    @XmlElement(name = "key_expiry_timestamp")
    public String getKeyExpiryTimestamp() {
	return keyExpiryTimestamp;
    }

    public void setKeyExpiryTimestamp(String keyExpiryTimestamp) {
	this.keyExpiryTimestamp = keyExpiryTimestamp;
    }

    public Link getLink() {
	return link;
    }

    public void setLink(Link link) {
	this.link = link;
    }

}