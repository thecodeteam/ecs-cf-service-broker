package com.emc.ecs.management.sdk.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data_service_vpool")
public class DataServiceReplicationGroup {
    private String description;
    private String name;
    private VArrayMapping varrayMappings;
    private Boolean isAllowAllNamespaces;
    private String id;
    private Link link;
    private String creationTime;
    private List<TagList> tags;
    private Boolean inactive;
    private Boolean global;
    private Boolean remote;
    private Vdc vdc;
    private Boolean internal;

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public VArrayMapping getVarrayMappings() {
	return varrayMappings;
    }

    public void setVarrayMappings(VArrayMapping varrayMappings) {
	this.varrayMappings = varrayMappings;
    }

    public Boolean getIsAllowAllNamespaces() {
	return isAllowAllNamespaces;
    }

    public void setIsAllowAllNamespaces(Boolean isAllowAllNamespaces) {
	this.isAllowAllNamespaces = isAllowAllNamespaces;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Link getLink() {
	return link;
    }

    public void setLink(Link link) {
	this.link = link;
    }

    @XmlAttribute(name = "creation_time")
    public String getCreationTime() {
	return creationTime;
    }

    public void setCreationTime(String creationTime) {
	this.creationTime = creationTime;
    }

    public List<TagList> getTags() {
	return tags;
    }

    public void setTags(List<TagList> tags) {
	this.tags = tags;
    }

    public Boolean getInactive() {
	return inactive;
    }

    public void setInactive(Boolean inactive) {
	this.inactive = inactive;
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

    public Vdc getVdc() {
	return vdc;
    }

    public void setVdc(Vdc vdc) {
	this.vdc = vdc;
    }

    public Boolean getInternal() {
	return internal;
    }

    public void setInternal(Boolean internal) {
	this.internal = internal;
    }
}
