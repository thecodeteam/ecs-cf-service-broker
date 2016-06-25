package com.emc.ecs.management.sdk.model;

public class BaseUrl {
    private String name;
    private String id;
    private Link link;

    public BaseUrl() {
        super();
    }

    public BaseUrl(String id, Link link, String name) {
        super();
        this.id = id;
        this.link = link;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
