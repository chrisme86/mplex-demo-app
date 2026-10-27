package com.cmiethling.mplex.device_events.api.brewer;

import static com.cmiethling.mplex.device_events.api.brewer.ErrorEvent.TOPIC;

import com.cmiethling.mplex.device_events.api.Destination;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * If there is an error or if the error is cleared this event is sent by the device.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Destination(AbstractBrewerDeviceEvent.destination + TOPIC)
public final class ErrorEvent extends AbstractBrewerDeviceEvent {

    public static final String TOPIC = "errors";
    public static final String ERRORCODE = "errorcode";

    private BrewerError error;

    /**
     * Creates a new event object.
     */
    public ErrorEvent() {
        super(TOPIC);
    }
}

