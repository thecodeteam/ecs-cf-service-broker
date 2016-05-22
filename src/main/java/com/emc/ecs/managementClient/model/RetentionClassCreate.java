package com.emc.ecs.managementClient.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "retention_class_create")
public class RetentionClassCreate extends RetentionClass {

    public RetentionClassCreate() {
	super();
    }

    public RetentionClassCreate(String name, int period) {
	setName(name);
	setPeriod(period);
    }

}
