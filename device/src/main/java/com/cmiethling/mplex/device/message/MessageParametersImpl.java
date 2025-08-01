package com.cmiethling.mplex.device.message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceMessageException;

/**
 * Implementation of the message parameters.
 */
public final class MessageParametersImpl implements MessageParameters {

    private final Map<String, Object> parameters;

    /**
     * Creates a new empty parameters object.
     */
    public MessageParametersImpl() {
        this.parameters = new HashMap<>();
    }

    @Override
    public Set<String> keySet() {
        return this.parameters.keySet();
    }

    @Override
    public boolean contains(final String name) {
        return this.parameters.containsKey(name);
    }

    @Override
    public Optional<Object> get(final String name) {
        return Optional.ofNullable(this.parameters.get(name));
    }

    @Override
    public Optional<String> getString(final String name) throws DeviceMessageException {
        try {
            return get(name).map(String.class::cast);
        } catch (final ClassCastException ex) {
            throw new DeviceMessageException("Error casting to String", ex, name);
        }
    }

    @Override
    public String getRequiredString(final String name) throws DeviceMessageException {
        return getString(name).orElseThrow(() -> new DeviceMessageException("missingValueForKey", name));
    }

    @Override
    public void putString(final String name, final String value) {
        if (value != null)
            this.parameters.put(name, value);
        else
            this.parameters.remove(name);
    }

    @Override
    public Optional<Boolean> getBoolean(final String name) throws DeviceMessageException {
        try {
            return get(name).map(Boolean.class::cast);
        } catch (final ClassCastException ex) {
            throw new DeviceMessageException("valueNotBoolean", ex, name);
        }
    }

    @Override
    public boolean getRequiredBoolean(final String name) throws DeviceMessageException {
        return getBoolean(name).orElseThrow(() -> new DeviceMessageException("missingValueForKey", name));
    }

    @Override
    public void putBoolean(final String name, final boolean value) {
        this.parameters.put(name, value);
    }

    @Override
    public OptionalInt getInt(final String name) throws DeviceMessageException {
        try {
            return get(name) //
                    .map(Number.class::cast) //
                    .map(number -> OptionalInt.of(number.intValue())) //
                    .orElseGet(OptionalInt::empty);
        } catch (final ClassCastException ex) {
            throw new DeviceMessageException("valueNotInteger", ex, name);
        }
    }

    @Override
    public int getRequiredInt(final String name) throws DeviceMessageException {
        return getInt(name).orElseThrow(() -> new DeviceMessageException("missingValueForKey", name));
    }

    @Override
    public void putInt(final String name, final int value) {
        this.parameters.put(name, value);
    }

    @Override
    public OptionalDouble getDouble(final String name) throws DeviceMessageException {
        try {
            return get(name) //
                    .map(Number.class::cast) //
                    .map(number -> OptionalDouble.of(number.doubleValue())) //
                    .orElseGet(OptionalDouble::empty);
        } catch (final ClassCastException ex) {
            throw new DeviceMessageException("valueNotDouble", ex, name);
        }
    }

    @Override
    public double getRequiredDouble(final String name) throws DeviceMessageException {
        return getDouble(name).orElseThrow(() -> new DeviceMessageException("missingValueForKey", name));
    }

    @Override
    public void putDouble(final String name, final double value) {
        this.parameters.put(name, value);
    }

    @Override
    public Optional<MessageParameters> getNested(final String name) throws DeviceMessageException {
        try {
            return get(name).map(MessageParameters.class::cast);
        } catch (final ClassCastException ex) {
            throw new DeviceMessageException("valueNotNested", ex, name);
        }
    }

    @Override
    public MessageParameters getRequiredNested(final String name) throws DeviceMessageException {
        return getNested(name).orElseThrow(() -> new DeviceMessageException("missingValueForKey", name));
    }

    @Override
    public MessageParameters addNested(final String name) {
        final var nested = new MessageParametersImpl();
        this.parameters.put(name, nested);
        return nested;
    }

    @Override
    public void putAll(final MessageParameters otherParameters) {
        for (final var parameter : otherParameters)
            this.parameters.put(parameter.getKey(), parameter.getValue());
    }

    @Override
    @NonNull
    public Iterator<Entry<String, Object>> iterator() {
        return this.parameters.entrySet().iterator();
    }

    @Override
    public void clear() {
        this.parameters.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final var other = (MessageParametersImpl) obj;

        // We need to implement our own Map comparison to handle numbers correctly.
        final var otherParameters = other.parameters;
        if (this.parameters.size() != otherParameters.size())
            return false;
        for (final var entry : this.parameters.entrySet()) {
            final var key = entry.getKey();
            final var value = entry.getValue();
            if (value == null) {
                if (!(otherParameters.get(key) == null && otherParameters.containsKey(key)))
                    return false;
            } else {
                if (value instanceof final Number number) {
                    final var otherValue = otherParameters.get(key);
                    if (!(otherValue instanceof final Number otherNumber))
                        return false;
                    if (Double.compare(number.doubleValue(), otherNumber.doubleValue()) != 0)
                        return false;
                } else if (!value.equals(otherParameters.get(key)))
                    return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return this.parameters.toString();
    }
}
