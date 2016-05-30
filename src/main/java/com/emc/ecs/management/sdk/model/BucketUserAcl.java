package com.emc.ecs.management.sdk.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user_acl")
public class BucketUserAcl {

    private String user;
    private List<String> permissions;

    public BucketUserAcl() {
	super();
    }

    public BucketUserAcl(String user, List<String> permissions) {
	super();
	this.user = user;
	this.permissions = permissions;
    }

    public String getUser() {
	return user;
    }

    public void setUser(String user) {
	this.user = user;
    }

    @XmlElement(name = "permission")
    public List<String> getPermissions() {
	return permissions;
    }

    public void setPermissions(List<String> permissions) {
	this.permissions = permissions;
    }
}