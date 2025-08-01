package com.cmiethling.mplex.device.message;

import java.util.stream.Stream;

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
    public static Subsystem valueOfId(final String id) {
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
    public String id() {
        return this.id;
    }
}

