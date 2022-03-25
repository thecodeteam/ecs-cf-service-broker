package com.emc.ecs.management.sdk.model.iam.user;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.emc.ecs.management.sdk.model.iam.TagResponse;
import com.emc.ecs.management.sdk.model.iam.common.TagsListResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ListUserTagsResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListUserTagsResponse")
public class ListUserTagsResponse extends BaseIamResponse {

    private TagsListResult result;

    public ListUserTagsResponse() {
    }

    public ListUserTagsResponse(List<TagResponse> tags, String nextMarker) {
        getResult().setTags(tags);
        getResult().setMarker(nextMarker);
    }

    @XmlElement(name = "ListUserTagsResult")
    @JsonProperty("ListUserTagsResult")
    public TagsListResult getResult() {
        if (result == null) {
            result = new TagsListResult();
        }
        return result;
    }

    public void setResult(TagsListResult result) {
        this.result = result;
    }
}
