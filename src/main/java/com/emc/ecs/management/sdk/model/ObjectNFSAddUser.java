package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "object_nfs_add_user")
public class ObjectNFSAddUser {
    private String namespace;
    private String type;
    private String name;
    private String id;
    private String mappingId;

    public ObjectNFSAddUser() {
        super();
    }

    public ObjectNFSAddUser(String namespace, String type, String user, String id, String mappingId) {
        super();
        this.namespace = namespace;
        this.type = type;
        this.name = user;
        this.id = id;
        this.mappingId = mappingId;
    }

    public String getNamespace() { return namespace; }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getMappingId() { return mappingId; }

    public void setMappingId(String mappingId) { this.mappingId = mappingId; }
}