package com.emc.ecs.managementClient.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "base_url")
public class BaseUrlInfo {
	private String id;
	private Link link;
	private String name;
	private List<String> tags;
	private String baseurl;
	private Boolean namespaceInHost = false;

	public BaseUrlInfo() {
		super();
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getBaseurl() {
		return baseurl;
	}

	public void setBaseurl(String baseurl) {
		this.baseurl = baseurl;
	}

	@XmlElement(name = "get_namespace_in_host")
	public Boolean getNamespaceInHost() {
		return namespaceInHost;
	}

	public void setNamespaceInHost(Boolean namespaceInHost) {
		this.namespaceInHost = namespaceInHost;
	}

	public String getNamespaceUrl(String namespace, boolean ssl) {
		String scheme = ssl ? "https" : "http";
		String port = ssl ? "9021" : "9020";

		if (namespaceInHost) {
			return scheme + "://" + namespace + "." + baseurl + ":" + port;
		} else {
			return scheme + "://" + baseurl + ":" + port;
		}
	}

}
