package io.ctrlplane.pilot.model.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {

    WRAP("keywrap"),
    UNWRAP("keyunwrap");

    public final String value;

    OperationType(final String value) {

        this.value = value;
    }

    @JsonCreator
    public OperationType fromValue(final String value) {

        for (OperationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {

        return this.value;
    }
}
