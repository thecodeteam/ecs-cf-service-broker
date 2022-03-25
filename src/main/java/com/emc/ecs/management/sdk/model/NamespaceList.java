package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "namespaces")
public class NamespaceList {
    private List<NamespaceInfo> namespaces;

    @XmlElement(name = "namespace")
    public List<NamespaceInfo> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<NamespaceInfo> namespaces) {
        this.namespaces = namespaces;
    }
}
