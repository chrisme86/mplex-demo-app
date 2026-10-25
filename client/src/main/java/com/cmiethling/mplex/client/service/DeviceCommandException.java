package com.cmiethling.mplex.client.service;

/**
 * A command was rejected by the device or the device could not be reached.
 */
public class DeviceCommandException extends RuntimeException {

    public DeviceCommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
