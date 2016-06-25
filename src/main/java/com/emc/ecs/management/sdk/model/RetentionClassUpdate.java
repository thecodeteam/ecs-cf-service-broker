package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "retention_class_update")
public class RetentionClassUpdate {

    private int period;

    public RetentionClassUpdate() {
        super();
    }

    public RetentionClassUpdate(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

}
