package com.cmiethling.mplex.device_events.api.heater;

import java.util.Optional;
import java.util.stream.Stream;

import com.cmiethling.mplex.device_events.api.SubsystemError;

/**
 * Each method referring to the heater interface provides a return value describing generic success or failure of the
 * command call.
 */
public enum HeaterError implements SubsystemError {

    NONE, OVERHEATED;

    /**
     * Returns the enum value associated with the specified code.
     *
     * @param code the code
     *
     * @return an optional enum value
     */
    public static Optional<HeaterError> ofCode(final int code) {
        return Stream.of(HeaterError.values()) //
                .filter(value -> value.code() == code) //
                .findFirst();
    }

    @Override
    public int code() {
        return this.ordinal();
    }
}

