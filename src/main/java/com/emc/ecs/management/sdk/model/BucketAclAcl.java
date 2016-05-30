package com.emc.ecs.management.sdk.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "acl")
public class BucketAclAcl {

    private List<BucketUserAcl> userAccessList;
    private List<BucketGroupAcl> groupAccessList;
    private List<BucketCustomGroupAcl> customGroupAccessList;

    public BucketAclAcl() {
	super();
    }

    public BucketAclAcl(List<BucketUserAcl> userAccessList,
	    List<BucketGroupAcl> groupAccessList,
	    List<BucketCustomGroupAcl> customGroupAccessList) {
	super();
	this.userAccessList = userAccessList;
	this.groupAccessList = groupAccessList;
	this.customGroupAccessList = customGroupAccessList;
    }

    @XmlElement(name = "user_acl")
    public List<BucketUserAcl> getUserAccessList() {
	return userAccessList;
    }

    public void setUserAccessList(List<BucketUserAcl> userAccessList) {
	this.userAccessList = userAccessList;
    }

    @XmlElement(name = "group_acl")
    public List<BucketGroupAcl> getGroupAccessList() {
	return groupAccessList;
    }

    public void setGroupAccessList(List<BucketGroupAcl> groupAccessList) {
	this.groupAccessList = groupAccessList;
    }

    @XmlElement(name = "custom_group_acl")
    public List<BucketCustomGroupAcl> getCustomGroupAccessList() {
	return customGroupAccessList;
    }

    public void setCustomGroupAccessList(
	    List<BucketCustomGroupAcl> customGroupAccessList) {
	this.customGroupAccessList = customGroupAccessList;
    }

}