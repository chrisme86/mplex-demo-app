package com.cmiethling.mplex.device.api.fluidics;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.EventMessage;

import lombok.Getter;

/**
 * If there is an error or if the error is cleared this event is sent by the device.
 */
@Getter
public final class ErrorEvent extends AbstractFluidicsDeviceEvent {

    public static final String TOPIC = "errors";
    public static final String ERRORCODE = "errorcode";

    private FluidicsError errorCode;

    /**
     * Creates a new event object.
     */
    public ErrorEvent() {
        super(TOPIC);
    }

    @Override
    public void fromEventMessage(@NonNull final EventMessage message) throws DeviceMessageException {
        super.fromEventMessage(message);

        final var code = message.parameters().getRequiredInt(ERRORCODE);
        this.errorCode = FluidicsError.ofCode(code).orElseThrow(() -> new DeviceMessageException(
                "eventUnknownErrorCode: message=%s, code=%s".formatted(message, code)));
    }
}

