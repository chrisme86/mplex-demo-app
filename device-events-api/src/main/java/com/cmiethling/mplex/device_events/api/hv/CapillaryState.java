package com.cmiethling.mplex.device_events.api.hv;

import java.util.stream.Stream;

import lombok.Getter;

/**
 * Describes the state of a capillary. This enum manages an additional index value instead of using
 * {@link Enum#ordinal()} to decouple the value from the position in the enum.
 */
@Getter
public enum CapillaryState {

    NONE(false, false), GOOD(true, false), BAD(false, true);

    private final boolean good;
    private final boolean bad;

    CapillaryState(final boolean good, final boolean bad) {
        this.good = good;
        this.bad = bad;
    }

    /**
     * Returns a CapillaryState.
     *
     * @param value the int the CapillaryState it represents.
     *
     * @return a CapillaryState object
     */
    public static CapillaryState ofValue(final int value) {
        return Stream.of(values()) //
                .filter(enumValue -> enumValue.ordinal() == value) //
                .findFirst() //
                .orElseThrow(() -> new IllegalArgumentException(CapillaryState.class.getSimpleName() + ": " + value));
    }
}
