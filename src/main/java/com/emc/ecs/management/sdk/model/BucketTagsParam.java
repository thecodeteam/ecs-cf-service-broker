package com.emc.ecs.management.sdk.model;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "add_bucket_tags")
@XmlAccessorType(XmlAccessType.FIELD)
public class BucketTagsParam {

    @XmlElementWrapper(name = "TagSet")
    @XmlElement(name = "Tag")
    private List<BucketTag> TagSet;

    private String namespace;

    public BucketTagsParam(){
        super();
    };

    public BucketTagsParam(String namespace, List<Map<String, String> > tags) {
        super();
        this.namespace = namespace;
        setTagSet(tags);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<BucketTag> getTagSet() {
        return TagSet;
    }

    public void setTagSet(List<Map<String, String> > tags) throws ServiceBrokerException {
        List<BucketTag> tagList = new ArrayList<BucketTag>();
        for (Map<String, String> tag: tags) {
            try {
                String key = tag.get("key");
                String value = tag.get("value");
                tagList.add(new BucketTag(key, value));
            } catch (Exception e) {
                throw new ServiceBrokerException("Key and Value should be specified for bucket tag", e);
            }
        }
        TagSet = tagList;
    }
}
