package com.cmiethling.mplex.device_events.api;

/**
 * This interface describes an event that could be sent by the device.
 */
public interface DeviceEvent {
    String prefix = "/topic";

    String getDestination();
}
