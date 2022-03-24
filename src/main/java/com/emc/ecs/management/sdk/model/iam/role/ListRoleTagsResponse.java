package com.emc.ecs.management.sdk.model.iam.role;

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

@XmlRootElement(name = "ListRoleTagsResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListRoleTagsResponse")
public class ListRoleTagsResponse extends BaseIamResponse {

    private TagsListResult result;

    public ListRoleTagsResponse() {
    }

    public ListRoleTagsResponse(List<TagResponse> tags, String nextMarker) {
        getResult().setTags(tags);
        getResult().setMarker(nextMarker);
    }

    @XmlElement(name = "ListRoleTagsResult")
    @JsonProperty("ListRoleTagsResult")
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
