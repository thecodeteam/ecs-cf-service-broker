package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class UserMapping {

    private String domain;
    private AttributeList attributes;
    private GroupList groups;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public AttributeList getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeList attributes) {
        this.attributes = attributes;
    }

    public GroupList getGroups() {
        return groups;
    }

    public void setGroups(GroupList groups) {
        this.groups = groups;
    }

    public static class Attribute {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class AttributeList {
        private List<Attribute> attributes;

        @XmlElement(name = "attribute")
        public List<Attribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }
    }

    public static class GroupList {
        private List<String> groups;

        @XmlElement(name = "group")
        public List<String> getGroups() {
            return groups;
        }

        public void setGroups(List<String> groups) {
            this.groups = groups;
        }
    }
}
