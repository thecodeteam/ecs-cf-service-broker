package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BucketTagsParam extends BucketTagSetRootElement{

    private String namespace;

    public BucketTagsParam(){
        super();
    };

    public BucketTagsParam(String namespace, List<Map<String, String> > tags) {
        super(tags);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
