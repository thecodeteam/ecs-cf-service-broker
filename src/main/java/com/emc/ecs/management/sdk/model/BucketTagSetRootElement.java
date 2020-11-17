package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class BucketTagSetRootElement {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    @XmlElementWrapper(name = "TagSet")
    @XmlElement(name = "Tag")
    private List<BucketTag> TagSet;

    public BucketTagSetRootElement(){};

    public BucketTagSetRootElement(List<Map<String, String>> tags) {
        super();
        setTagSetAsListOfMaps(tags);
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
                put(KEY, tag.getKey());
                put(VALUE, tag.getValue());
            }};
            list.add(map);
        }
        return list;
    }

    public void setTagSetAsListOfMaps(List<Map<String, String> > tags) throws IllegalArgumentException {
        List<BucketTag> tagList = new ArrayList<BucketTag>();
        for (Map<String, String> tag: tags) {
            try {
                String key = tag.get(KEY);
                String value = tag.get(VALUE);
                tagList.add(new BucketTag(key, value));
            } catch (Exception e) {
                throw new IllegalArgumentException("Key and Value should be specified for bucket tag", e);
            }
        }
        TagSet = tagList;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("{");
        for (BucketTag tag: TagSet) {
            output.append(tag.toString()).append(", ");
        }
        int length = output.length();
        if (length != 1)
            output.delete(length - 2, length);
        output.append("}");
        return output.toString();
    }
}
