package com.cmiethling.mplex.device_events.api;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the subsystems available in the hardware interface.
 */
public enum Subsystem {

    HIGH_VOLTAGE("highvoltage"), MOTOR_CONTROL("motorcontrol"), FLUIDICS("fluidics"), TEST("test");

    private final String id;

    Subsystem(final String id) {
        this.id = id;
    }

    /**
     * Returns the enum value that is associated with the specified id. If no enum value can be found for this id an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param id the id to look for
     *
     * @return the value associated with this id
     *
     * @throws IllegalArgumentException if no enum value can be found for this id
     */
    @JsonCreator // used by Jackson to deserialize the id
    public static Subsystem fromJson(final String id) {
        return Stream.of(Subsystem.values()) //
                .filter(value -> value.id().equals(id)) //
                .findFirst() //
                .orElseThrow(() -> new IllegalArgumentException(id));
    }

    /**
     * Returns the id of the subsystem that is used in communication.
     *
     * @return the id of the subsystem
     */
    @JsonValue
    public String id() {
        return this.id;
    }
}

