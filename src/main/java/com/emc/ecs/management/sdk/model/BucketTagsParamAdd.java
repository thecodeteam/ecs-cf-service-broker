package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "add_bucket_tags")
public class BucketTagsParamAdd extends BucketTagsParam {
    
    public BucketTagsParamAdd(){
        super();
    };

    public BucketTagsParamAdd(String namespace, List<Map<String, String>> tags) {
        super(namespace, tags);
    }
}
