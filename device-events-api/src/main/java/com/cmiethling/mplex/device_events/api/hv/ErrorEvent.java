package com.cmiethling.mplex.device_events.api.hv;

import static com.cmiethling.mplex.device_events.api.hv.ErrorEvent.TOPIC;

import com.cmiethling.mplex.device_events.api.AbstractDeviceEvent;
import com.cmiethling.mplex.device_events.api.Destination;
import com.cmiethling.mplex.device_events.api.DeviceEvent;
import com.cmiethling.mplex.device_events.api.Subsystem;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * If there is an error or if the error is cleared this event is sent by the device.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Destination(DeviceEvent.prefix + "/HIGH_VOLTAGE." + TOPIC)
public final class ErrorEvent extends AbstractDeviceEvent {

    public static final String TOPIC = "errors";
    public static final String ERRORCODE = "errorcode";

    private HighVoltageError error;

    /**
     * Creates a new event object.
     */
    public ErrorEvent() {
        super(Subsystem.HIGH_VOLTAGE, TOPIC);
    }
}

