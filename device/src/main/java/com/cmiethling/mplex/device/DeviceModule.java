package com.cmiethling.mplex.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to the device module. It can be used to get information about the module and access
 * resources.
 */
public final class DeviceModule {

    /**
     * Defines the name of the module in order to find it in the module layer.
     */
    public static final String NAME = "com.cmiethling.mplex.device";

    private DeviceModule() {
        // hidden
    }

    /**
     * Returns the default logger for this module.
     *
     * @return the default logger
     */
    public static Logger logger() {
        return LoggerFactory.getLogger(NAME);
    }

    /**
     * Return a logger for the specified class. The package name of the class is used as the logger name.
     *
     * @param sourceClass the source class of the logging
     *
     * @return the logger
     */
    public static Logger logger(final Class<?> sourceClass) {
        return logger(sourceClass.getPackageName());
    }

    /**
     * Return a logger with the specified name. The specified name is added to the name of the module logger.
     *
     * @param name the name of the logger
     *
     * @return the logger
     */
    public static Logger logger(final String name) {
        return LoggerFactory.getLogger("%s.%s".formatted(NAME, name));
    }
}
