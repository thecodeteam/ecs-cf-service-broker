package com.emc.ecs.management.sdk.model.iam.role;

import com.emc.ecs.management.sdk.model.iam.BaseIamResponse;
import com.emc.ecs.management.sdk.model.iam.IamResponseConstants;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ListRolesResponse", namespace = IamResponseConstants.RESPONSE_XML_NAMESPACE)
@XmlAccessorType(value = XmlAccessType.PROPERTY)
@JsonRootName("ListRolesResponse")
public class ListRolesResponse extends BaseIamResponse {
    private ListRolesResult result;

    public ListRolesResponse() {
    }

    public ListRolesResponse(List<IamRole> roles, String nextMarker) {
        this.getResult().setRoles(roles);
        this.getResult().setMarker(nextMarker);
        this.getResult().setTruncated(nextMarker != null && !nextMarker.isEmpty());
    }

    @XmlElementRef
    public ListRolesResult getResult() {
        if (result == null) {
            result = new ListRolesResult();
        }
        return result;
    }

    public void setResult(ListRolesResult result) {
        this.result = result;
    }
}
