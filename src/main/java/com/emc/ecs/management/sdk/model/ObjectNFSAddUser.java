package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

import static com.emc.ecs.management.sdk.Constants.USER;

@XmlRootElement(name = "object_nfs_add_user")
public class ObjectNFSAddUser {
    private String user;
    private String id;
    private String type;
    private String namespace;
    private String mappingId;

    public ObjectNFSAddUser() {
        super();
    }

    public ObjectNFSAddUser(String user, String id, String namespace, String mappingId) {
        super();
        this.user = user;
        this.id = id;
        this.type = USER;
        this.namespace = namespace;
        this.mappingId = mappingId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getNamespace() { return namespace; }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMappingId() { return mappingId; }

    public void setMappingId(String id) { this.mappingId = id; }


}