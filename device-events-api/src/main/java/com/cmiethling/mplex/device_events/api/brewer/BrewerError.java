package com.cmiethling.mplex.device_events.api.brewer;

import java.util.Optional;
import java.util.stream.Stream;

import com.cmiethling.mplex.device_events.api.SubsystemError;

/**
 * Each method referring to the brewer interface provides a return value describing generic success or failure of the
 * command call.
 */
public enum BrewerError implements SubsystemError {

    NONE, PUMP_JAMMED;

    /**
     * Returns the enum value associated with the specified code.
     *
     * @param code the code
     *
     * @return an optional enum value
     */
    public static Optional<BrewerError> ofCode(final int code) {
        return Stream.of(BrewerError.values()) //
                .filter(value -> value.code() == code) //
                .findFirst();
    }

    @Override
    public int code() {
        return this.ordinal();
    }
}

