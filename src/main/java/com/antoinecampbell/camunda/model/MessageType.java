package com.antoinecampbell.camunda.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

public enum MessageType {
    EXTERNAL_TASK("external-task");

    @JsonValue
    @Getter
    private final String name;

    MessageType(String name) {
        this.name = name;
    }

    public static MessageType fromString(String name) {
        return Stream.of(MessageType.values())
                .filter(messageType -> messageType.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
