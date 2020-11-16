package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "update_bucket_tags")
public class BucketTagsParamUpdate extends BucketTagsParam {

    public BucketTagsParamUpdate(){
        super();
    };

    public BucketTagsParamUpdate(String namespace, List<Map<String, String>> tags) {
        super(namespace, tags);
    }
}
