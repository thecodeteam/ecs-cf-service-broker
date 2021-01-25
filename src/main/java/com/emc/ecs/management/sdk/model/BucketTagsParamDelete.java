package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "delete_bucket_tags")
public class BucketTagsParamDelete extends BucketTagsParam {

    public BucketTagsParamDelete(){
        super();
    };

    public BucketTagsParamDelete(String namespace, List<Map<String, String>> tags) {
        super(namespace, tags);
    }
}
