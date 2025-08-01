package com.cmiethling.mplex.device.message;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

import com.cmiethling.mplex.device.DeviceMessageException;

/**
 * Defines the access to the parameters of a device message.
 */
public interface MessageParameters extends Iterable<Map.Entry<String, Object>> {

    /**
     * Returns the names of all parameters.
     *
     * @return a set with names or an empty set
     */
    Set<String> keySet();

    /**
     * Checks if there is a parameter value for the given name.
     *
     * @param name the name of the parameter
     *
     * @return {@code true} if there is a value, otherwise {@code false}
     */
    boolean contains(String name);

    /**
     * Returns an optional parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return an optional value
     */
    Optional<Object> get(String name);

    /**
     * Returns an optional string parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return an optional value
     *
     * @throws DeviceMessageException if the parameter value is not a string
     */
    Optional<String> getString(String name) throws DeviceMessageException;

    /**
     * Returns a required string parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return the value
     *
     * @throws DeviceMessageException if there is no such parameter or if the parameter value is not a string
     */
    String getRequiredString(String name) throws DeviceMessageException;

    /**
     * Sets a string parameter value for the given name. Any existing parameter value will be replaced.
     *
     * @param name  the name of the parameter
     * @param value the new parameter value
     */
    void putString(String name, String value);

    /**
     * Returns an optional boolean parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return an optional value
     *
     * @throws DeviceMessageException if the parameter value is not a boolean
     */
    Optional<Boolean> getBoolean(String name) throws DeviceMessageException;

    /**
     * Returns a required boolean parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return the value
     *
     * @throws DeviceMessageException if there is no such parameter or if the parameter value is not a boolean
     */
    boolean getRequiredBoolean(String name) throws DeviceMessageException;

    /**
     * Sets a boolean parameter value for the given name. Any existing parameter value will be replaced.
     *
     * @param name  the name of the parameter
     * @param value the new parameter value
     */
    void putBoolean(String name, boolean value);

    /**
     * Returns an optional integer parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return an optional value
     *
     * @throws DeviceMessageException if the parameter value is not an integer
     */
    OptionalInt getInt(String name) throws DeviceMessageException;

    /**
     * Returns a required int parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return the value
     *
     * @throws DeviceMessageException if there is no such parameter or if the parameter value is not an int
     */
    int getRequiredInt(String name) throws DeviceMessageException;

    /**
     * Sets an int parameter value for the given name. Any existing parameter value will be replaced.
     *
     * @param name  the name of the parameter
     * @param value the new parameter value
     */
    void putInt(String name, int value);

    /**
     * Returns an optional double parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return an optional value
     *
     * @throws DeviceMessageException if the parameter value is not a double
     */
    OptionalDouble getDouble(String name) throws DeviceMessageException;

    /**
     * Returns a required double parameter for the given name.
     *
     * @param name the name of the parameter
     *
     * @return the value
     *
     * @throws DeviceMessageException if there is no such parameter or if the parameter value is not a double
     */
    double getRequiredDouble(String name) throws DeviceMessageException;

    /**
     * Sets a double parameter value for the given name. Any existing parameter value will be replaced.
     *
     * @param name  the name of the parameter
     * @param value the new parameter value
     */
    void putDouble(String name, double value);

    /**
     * Returns optional nested parameters for the given name.
     *
     * @param name the name of the parameters
     *
     * @return an optional nested parameters
     *
     * @throws DeviceMessageException if the parameter value does not represent nested parameters
     */
    Optional<MessageParameters> getNested(String name) throws DeviceMessageException;

    /**
     * Returns required nested parameters for the given name.
     *
     * @param name the name of the parameters
     *
     * @return the nested parameters
     *
     * @throws DeviceMessageException if there is no such parameter or if the parameter value does not represent nested
     *                                parameters
     */
    MessageParameters getRequiredNested(String name) throws DeviceMessageException;

    /**
     * Adds new nested parameters for the given name. Any existing parameter value will be replaced.
     *
     * @param name the name of the parameters
     *
     * @return the newly added empty parameters
     */
    MessageParameters addNested(String name);

    /**
     * Sets all parameters from {@code parameters} to this parameter object. Any existing parameter values will be
     * replaced.
     *
     * @param otherParameters the parameters to read from
     */
    void putAll(MessageParameters otherParameters);

    /**
     * Removes all parameter values.
     */
    void clear();
}

