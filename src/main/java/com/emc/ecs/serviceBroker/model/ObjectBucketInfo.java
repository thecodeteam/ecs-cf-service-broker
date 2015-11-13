package com.emc.ecs.serviceBroker.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "object_bucket_info")
public class ObjectBucketInfo {

	private String id;
	private String create;
	private String softquota;
	private Boolean fsAccessEnabled;
	private Boolean locked;
	private String vpool;
	private String namespace;
	private String owner;
	private Boolean isStaleAllowed;
	private long defaultRetention;
	private long blockSize;
	private long notificationSize;
	private String apiType;
	private Link link;
	private Boolean global;
	private Boolean remote;
	private Boolean internal;
	private Boolean inactive;
	private String name;
	private Vdc vdc;
	private ArrayList<String> tags;
	
	public ObjectBucketInfo() {
		super();
	}

	public String getCreate() {
		return create;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public String getSoftquota() {
		return softquota;
	}

	public void setSoftquota(String softquota) {
		this.softquota = softquota;
	}

	@XmlElement(name = "fs_access_enabled")
	public Boolean getFsAccessEnabled() {
		return fsAccessEnabled;
	}

	public void setFsAccessEnabled(Boolean fsAccessEnabled) {
		this.fsAccessEnabled = fsAccessEnabled;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
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

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@XmlElement(name = "is_stale_allowed")
	public Boolean getIsStaleAllowed() {
		return isStaleAllowed;
	}

	public void setIsStaleAllowed(Boolean isStaleAllowed) {
		this.isStaleAllowed = isStaleAllowed;
	}

	@XmlElement(name = "default_retention")
	public long getDefaultRetention() {
		return defaultRetention;
	}

	public void setDefaultRetention(long defaultRetention) {
		this.defaultRetention = defaultRetention;
	}

	@XmlElement(name = "block_size")
	public long getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}

	@XmlElement(name = "notification_size")
	public long getNotificationSize() {
		return notificationSize;
	}

	public void setNotificationSize(long notificationSize) {
		this.notificationSize = notificationSize;
	}

	@XmlElement(name = "api_type")
	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Boolean getGlobal() {
		return global;
	}

	public void setGlobal(Boolean global) {
		this.global = global;
	}

	public Boolean getRemote() {
		return remote;
	}

	public void setRemote(Boolean remote) {
		this.remote = remote;
	}

	public Boolean getInternal() {
		return internal;
	}

	public void setInternal(Boolean internal) {
		this.internal = internal;
	}

	public Vdc getVdc() {
		return vdc;
	}

	public void setVdc(Vdc vdc) {
		this.vdc = vdc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getInactive() {
		return inactive;
	}

	public void setInactive(Boolean inactive) {
		this.inactive = inactive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
}
