package com.emc.ecs.management.sdk.model;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
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
        setTagSetAsListOfMaps(tags);
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

    public void setTagSet(List<BucketTag> tagSet) {
        TagSet = tagSet;
    }

    public List<Map<String, String> > getTagSetAsListOfTags() {
        List<Map<String, String> > list = new ArrayList<Map<String, String> >();
        for (BucketTag tag: TagSet) {
            Map<String, String> map = new HashMap<String, String>() {{
                put("key", tag.getKey());
                put("value", tag.getValue());
            }};
            list.add(map);
        }
        return list;
    }

    public void setTagSetAsListOfMaps(List<Map<String, String> > tags) throws ServiceBrokerException {
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
