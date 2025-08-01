package com.cmiethling.mplex.device;

public final class DeviceCommunicationException extends DeviceException {

    public DeviceCommunicationException(final String message, final Exception exception, final Object... args) {
        super(message, exception, args);
    }

    @Override
    public String getId() {
        return "COM";
    }
}
