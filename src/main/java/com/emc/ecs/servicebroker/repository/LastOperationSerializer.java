package com.emc.ecs.servicebroker.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.cloud.servicebroker.model.instance.OperationState;

import java.io.IOException;

@SuppressWarnings("unused")
public class LastOperationSerializer {

    @JsonSerialize
    @JsonDeserialize(using = OperationStateDeserializer.class)
    @JsonProperty("operation_state")
    private OperationState operationState;

    @JsonSerialize
    private String description;

    @JsonSerialize
    @JsonProperty("delete_operation")
    private boolean deleteOperation;

    public LastOperationSerializer() {
        super();
    }

    public LastOperationSerializer(final OperationState operationState, final String description, final boolean deleteOperation) {
        super();
        this.setOperationState(operationState);
        this.setDescription(description);
        this.setDeleteOperation(deleteOperation);
    }

    public OperationState getOperationState() {
        return operationState;
    }

    private void setOperationState(OperationState operationState) {
        this.operationState = operationState;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleteOperation() {
        return deleteOperation;
    }

    private void setDeleteOperation(boolean deleteOperation) {
        this.deleteOperation = deleteOperation;
    }

    /**
     * This deserializer supports OperationState enum values from both 1.x and 2.x versions of Spring Cloud Open Service Broker
     */
    public static class OperationStateDeserializer extends StdDeserializer<OperationState> {
        @Override
        public OperationState deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);
            String inValue = node.textValue();
            try {
                return OperationState.valueOf(inValue);
            } catch (IllegalArgumentException e) {
                if (inValue != null) {
                    for (OperationState value : OperationState.values()) {
                        if (inValue.equals(value.getValue())) {
                            return value;
                        }
                    }
                }
            }

            return null;
        }

        public OperationStateDeserializer() {
            super(OperationState.class);
        }
    }
}
